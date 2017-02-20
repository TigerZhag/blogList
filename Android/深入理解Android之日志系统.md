# 前记
在Android开发中,logcat是我们不可或缺的调试工具,我一直有个疑问,logcat的到底是在哪里存储着呢,带着这个疑问,开始探究Android日志系统.
> 参考文献: [Android日志系统驱动程序Logger源代码分析](Http://blog.csdn.net/luoshengyang/article/details/6595744)
> 没有下载源码的同学可以参考 [Google git](https://android.googlesource.com/kernel/common/+/19cada644d55214b6d08e4a8b2345eac1c479167/drivers/staging/android)

# 概述
Android的日志系统是一个设备,在类Unix中,设备不一定要对应于物理设备,叫做伪设备.
我们知道Linux系统中万物皆文件,设备也是一种文件,即类型为c或b的文件,在/dev/log/目录下,我们可以找到几个文件:

![Andoid_log_device.png](http://upload-images.jianshu.io/upload_images/1448134-450c2c2e1e4af4ee.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)


日志的读写总体流程:
1. 开机之后,Logger的初始化
2. 将Log写入RingBuffer
   2.1 App(包括使用NDK)调用Logger输出log
   2.2 Logger将Log写入RingBuffer
3. 读取RingBuffer 
   3.1 shell中使用logcat
   3.2 Logger读取RingBuffer

# RingBuffer的结构
首先需要了解一下RingBuffer中存储Log时的数据结构,方便下文的理解
总体结构:


Logger中持有RingBuffer的Logger_log:
```c
//  kernel/common/drivers/staging/android/logger.c

struct logger_log{
    unsigned char * buffer;   //持有的RingBuffer缓冲区
    struct miscdevice misc;   //Logger使用的设备,可看出Logger属于misc(混杂设备)
    wait_quene_head_t wq;     //用于保存正在等待读取日志的进程
    struct list_head readers; //正在读去日志的进程
    struct mutex mutex;       //一个保护log并发访问的互斥量
    size_t w_off;             //当前写入位置的偏移(即下一条往哪写)
    size_t head;              //读取时应该从RingBuffer环形缓冲的哪个位置开始读
    size_t size;              //log的size
}
```

Ringbuffer中的基本单元Logger_entry:
```c
// kernel/common/drivers/staging/android/logger.h

struct logger_entry{
    __u16       len;    // 有效负载的长度
    __u16       __pad;  // 对齐结构体
    __s32       pid;    // log所属的进程
    __s32       tid;    // log所属的线程
    __s32       sec;    // log发出的时间
    __s32       nsec;   // log发出的纳秒时间
    char        msg[0]; // 有效负载
}
```

读取Log的结构体:
```c
struct logger_reader {
    struct logger_log * log;    // 指向一个Logger设备 如main/system/event
    struct list_head    list;   // Logger_log中的
    size_t              r_off;  // 当前读取位置的偏移
   	bool			    r_all;	// reader能否读取所有log
	int			        r_ver;	// reader ABI版本
};
```

# Logger的初始化过程
分为三步:
1. 定义
2. 初始化
3. 注册

## 定义四个日志设备
```c
/*
 * Defines a log structure with name 'NAME' and a size of 'SIZE' bytes, which
 * must be a power of two, and greater than
 * (LOGGER_ENTRY_MAX_PAYLOAD + sizeof(struct logger_entry)).
 */
#define DEFINE_LOGGER_DEVICE(VAR, NAME, SIZE) \
static unsigned char _buf_ ## VAR[SIZE]; \
static struct logger_log VAR = { \
	.buffer = _buf_ ## VAR, \
	.misc = { \
		.minor = MISC_DYNAMIC_MINOR, \
		.name = NAME, \
		.fops = &logger_fops, \
		.parent = NULL, \
	}, \
	.wq = __WAIT_QUEUE_HEAD_INITIALIZER(VAR .wq), \
	.readers = LIST_HEAD_INIT(VAR .readers), \
	.mutex = __MUTEX_INITIALIZER(VAR .mutex), \
	.w_off = 0, \
	.head = 0, \
	.size = SIZE, \
};

DEFINE_LOGGER_DEVICE(log_main, LOGGER_LOG_MAIN, 256*1024)
DEFINE_LOGGER_DEVICE(log_events, LOGGER_LOG_EVENTS, 256*1024)
DEFINE_LOGGER_DEVICE(log_radio, LOGGER_LOG_RADIO, 256*1024)
DEFINE_LOGGER_DEVICE(log_system, LOGGER_LOG_SYSTEM, 256*1024)

static struct logger_log *get_log_from_minor(int minor)
{
	if (log_main.misc.minor == minor)
		return &log_main;
	if (log_events.misc.minor == minor)
		return &log_events;
	if (log_radio.misc.minor == minor)
		return &log_radio;
	if (log_system.misc.minor == minor)
		return &log_system;
	return NULL;
}
```

## 初始化日志设备
```c
static int __init logger_init(void)
{
	int ret;
	ret = init_log(&log_main);
	if (unlikely(ret))
		goto out;
	ret = init_log(&log_events);
	if (unlikely(ret))
		goto out;
	ret = init_log(&log_radio);
	if (unlikely(ret))
		goto out;
	ret = init_log(&log_system);
	if (unlikely(ret))
		goto out;
out:
	return ret;
}
device_initcall(logger_init);
```

## 注册设备
注册设备的源码在kernel/common/drivers/char/misc.c中,可以在Google Git中查看
```c
int misc_register(struct miscdevice * misc)
{
		struct miscdevice *c;
		dev_t dev;
		int err = 0;

		INIT_LIST_HEAD(&misc->list);

		mutex_lock(&misc_mtx);
		list_for_each_entry(c, &misc_list, list) {
				if (c->minor == misc->minor) {
						mutex_unlock(&misc_mtx);
						return -EBUSY;
				}
		}

		if (misc->minor == MISC_DYNAMIC_MINOR) {
				int i = DYNAMIC_MINORS;
				while (--i >= 0)
						if ( (misc_minors[i>>3] & (1 << (i&7))) == 0)
								break;
				if (i<0) {
						mutex_unlock(&misc_mtx);
						return -EBUSY;
				}
				misc->minor = i;
		}

		if (misc->minor < DYNAMIC_MINORS)
				misc_minors[misc->minor >> 3] |= 1 << (misc->minor & 7);
		dev = MKDEV(MISC_MAJOR, misc->minor);

		misc->this_device = device_create(misc_class, misc->parent, dev, NULL,
                                          "%s", misc->name);
        if (IS_ERR(misc->this_device)) {
                err = PTR_ERR(misc->this_device);
                goto out;
        }

        /*
         * Add it to the front, so that later devices can "override"
         * earlier defaults
         */
        list_add(&misc->list, &misc_list);
 out:
        mutex_unlock(&misc_mtx);
        return err;
}
```

注册之后的设备就可以在/dev/log/下找到了,并且用户空间即可读写这些设备和驱动程序进行交互.

# 写入过程
写入过程分为两个环节, 首先是用户空间调用Logger打印日志, 然后是Logger写入日志的实现原理
## APP(NDK)调用Logger输出Log
### 使用java代码调用log
这种方式大家肯定都很熟悉, 不再多说.
### 在NDK中打印log
1. 定义自己的 LOG_TAG 宏;
2. 包含头文件 system/core/include/cutils/log.h ;
代码示例:

```c
#define LOG_TAG "MY LOG TAG"
#include <cutils/log.h>

LOGV("This is the log printed by LOGV in android user space.");
```

日志级别跟java接口差不多:

```c
/* 
 * Android log priority values, in ascending priority order. 
 */  
typedef enum android_LogPriority {  
    ANDROID_LOG_UNKNOWN = 0,  
    ANDROID_LOG_DEFAULT,    /* only for SetMinPriority() */  
    ANDROID_LOG_VERBOSE,  
    ANDROID_LOG_DEBUG,  
    ANDROID_LOG_INFO,  
    ANDROID_LOG_WARN,  
    ANDROID_LOG_ERROR,  
    ANDROID_LOG_FATAL,  
    ANDROID_LOG_SILENT, /* only for SetMinPriority(); must be last */  
} android_LogPriority;  
```

## Logger写入RingBuffer
用户空间调用了打印log的方法之后, logger就会将其写入到RingBuffer中,主要分为五步:
1. 构造需要写入RingBuffer的结构体;
2. 如果日志覆盖了之前的, 调整读写指针;
3. 调用do_write_log把logger_entry写入RingBuffer;
4. 调用do_write_log_from_user把priority,tag, msg写入RingBuffer;
5. 唤醒阻塞等待日志更新的reader进程;

```c
/* 
 * logger_aio_write - our write method, implementing support for write(), 
 * writev(), and aio_write(). Writes are our fast path, and we try to optimize 
 * them above all else. 
 */  
ssize_t logger_aio_write(struct kiocb *iocb, const struct iovec *iov,  
             unsigned long nr_segs, loff_t ppos)  
{  
    //第一步
    struct logger_log *log = file_get_log(iocb->ki_filp);  
    size_t orig = log->w_off;  
    struct logger_entry header;  
    struct timespec now;  
    ssize_t ret = 0;  
  
    now = current_kernel_time();  
  
    header.pid = current->tgid;  
    header.tid = current->pid;  
    header.sec = now.tv_sec;  
    header.nsec = now.tv_nsec;  
    header.len = min_t(size_t, iocb->ki_left, LOGGER_ENTRY_MAX_PAYLOAD);  
  
    /* null writes succeed, return zero */  
    if (unlikely(!header.len))  
        return 0;  
  
    mutex_lock(&log->mutex);
  
    //第二步
    /* 
     * Fix up any readers, pulling them forward to the first readable 
     * entry after (what will be) the new write offset. We do this now 
     * because if we partially fail, we can end up with clobbered log 
     * entries that encroach on readable buffer. 
     */  
    fix_up_readers(log, sizeof(struct logger_entry) + header.len);  
  
    //第三步
    do_write_log(log, &header, sizeof(struct logger_entry));  
  
    while (nr_segs-- > 0) {  
        size_t len;  
        ssize_t nr;  
  
        /* figure out how much of this vector we can keep */  
        len = min_t(size_t, iov->iov_len, header.len - ret);  
  
    /* write out this segment's payload */
        //第四步
        nr = do_write_log_from_user(log, iov->iov_base, len);  
        if (unlikely(nr < 0)) {  
            log->w_off = orig;  
            mutex_unlock(&log->mutex);  
            return nr;  
        }  
  
        iov++;  
        ret += nr;  
    }  
  
    mutex_unlock(&log->mutex);  
  
    //第五步
    /* wake up any blocked readers */  
    wake_up_interruptible(&log->wq);  
  
    return ret;  
}
```
每个要写入的日志的结构形式:
struct logger_entry | priority | tag | msg
其中,priority、tag 和 msg 这三个段的内容是由 iov 参数从用户空间传递下来的，分别对应 iov 里面的三个元素。而 logger_entry 是由内核空间来构造的,

```c
 struct logger_entry header;
	struct timespec now;

	now = current_kernel_time();

	header.pid = current->tgid;
	header.tid = current->pid;
	header.sec = now.tv_sec;
	header.nsec = now.tv_nsec;
	header.len = min_t(size_t, iocb->ki_left, LOGGER_ENTRY_MAX_PAYLOAD);
```

# 读取
## Shell中使用logcat
先看logcat的用法, 查看logcat --help: 一般用法 [adb] logcat [<option>] ... [<filter-spec>] ...
参数包括:

```shell
  -s              设置过滤器,过滤标签
  -f <filename>   把缓存输出到<filename>, 如果不设置默认输出到stdout, 在Shell中也可以 adb logcat > filename来重定向到filename文件中
  -r [<kbytes>]   日志文件的最大尺寸,需要和-f配合使用,单位为KB. 超过这个尺寸后将原文件转移到filename.1,若有filename.1则将其转移到filename.2,以此类推.默认16KB
  -n <count>      日志文件的最多个数,默认为4
  -v <format>     设置日志输出格式 Brief process tag thread raw time threadtime long
                  默认格式brief: 优先级/标签(进程ID):日志信息
                  process格式: 优先级 (进程ID) : 日志信息
                  tag格式: 优先级 / 标签 : 日志信息
                  thread格式: 优先级 ( 进程ID : 线程ID) 标签 : 日志内容
                  raw格式: 只输出日志信息, 不附加任何其他 信息, 如 优先级 标签等
                  time格式: 日期 时间 优先级 / 标签 (进程ID) : 进程名称 : 日志信息

  -C              清除缓存
  -d              将ring buffer中所有内容输出后直接退出,不阻塞
  -t <count>      输出最近的count条日志,不阻塞
  -t '<time>'     输出time之后的日志,不阻塞 时间格式 'MM-DD hh:mm:ss.mmm'
  -T <count>      阻塞版 -t
  -T '<time>'     阻塞版 -t
  -g              获取log设备的ring buffer大小
  -b <buffer>     指定使用的日志缓冲区, 可选多个, 默认 -b main -b system -b crash,还可以选择:events, crash or all.
  -B              输出二进制
  -S              输出统计信息
  -G <size>       设置Ring Buffer的尺寸,单位为K或M
  -p              print prune white and ~black list. Service is specified as
                  UID, UID/PID or /PID. Weighed for quicker pruning if prefix
                  with ~, otherwise weighed for longevity if unadorned. All
                  other pruning activity is oldest first. Special case ~!
                  represents an automatic quicker pruning for the noisiest
                  UID as determined by the current statistics.
  -P '<list> ...' set prune white and ~black list, using same format as
                  printed above. Must be quoted.
```

> -p和-P两个参数没看懂不知道怎么用,网上也没查到,希望知道的大神不吝赐教..

过滤项解析 filter-spec:
格式: <tag>:[priority], 默认的过滤项为 " *:I ";
优先级级别:
--V : Verbose
--D : Debug
--I : Info
--W : warning
--E : Error
--F : Fatal
--S : Super all outputs

可设置多个过滤项, 如 adb logcat ActivityManager:D dalvikvm:I *:S,做或运算,注意,要有 *:S才能正确输出

> 当然还可以在终端中使用重定向 > 和grep等命令来过滤

## Logger的读取过程
Logger从RingBuffer中读取日志的过程只要分为:
1. 打开设备文件;
2. 判断是否有新日志可读, 判断方法:读写指针是否在同一位置. 若无新日志,根据打开方式判断是否需要休眠,等待唤醒;
3. 有新日志, 读取日志
4. 调整指针

 其中在第三步在日志的读取过程, 又分为:
3.1 获取有效负载长度, 在logger_entry中的前两个字节, 需要判断是否一个在首一个在尾;
3.2 logger_entry长度固定,可得日志记录总长度;
3.3 调用do_read_log_to_user函数来执行真正的读取动作(把内核空间的RingBuffer指定内容拷贝到用户空间的内存缓冲区即可);

代码如下:
```cpp
/* 
 * logger_read - our log's read() method 
 * 
 * Behavior: 
 * 
 *  - O_NONBLOCK works 
 *  - If there are no log entries to read, blocks until log is written to 
 *  - Atomically reads exactly one log entry 
 * 
 * Optimal read size is LOGGER_ENTRY_MAX_LEN. Will set errno to EINVAL if read 
 * buffer is insufficient to hold next entry. 
 */  
static ssize_t logger_read(struct file *file, char __user *buf,  
               size_t count, loff_t *pos)  
{  
    struct logger_reader *reader = file->private_data;  
    struct logger_log *log = reader->log;  
    ssize_t ret;  
    DEFINE_WAIT(wait);  
  
start:  
    while (1) {  
        prepare_to_wait(&log->wq, &wait, TASK_INTERRUPTIBLE);  
  
        mutex_lock(&log->mutex);  
        ret = (log->w_off == reader->r_off);  
        mutex_unlock(&log->mutex);  
        if (!ret)  
            break;  
  
        if (file->f_flags & O_NONBLOCK) {  
            ret = -EAGAIN;  
            break;  
        }  
  
        if (signal_pending(current)) {  
            ret = -EINTR;  
            break;  
        }  
  
        schedule();  
    }  
  
    finish_wait(&log->wq, &wait);  
    if (ret)  
        return ret;  
  
    mutex_lock(&log->mutex);  
  
    /* is there still something to read or did we race? */  
    if (unlikely(log->w_off == reader->r_off)) {  
        mutex_unlock(&log->mutex);  
        goto start;  
    }  
  
    /* get the size of the next entry */  
    ret = get_entry_len(log, reader->r_off);  
    if (count < ret) {  
        ret = -EINVAL;  
        goto out;  
    }  
  
    /* get exactly one entry from the log */  
    ret = do_read_log_to_user(log, reader, buf, ret);  
  
out:  
    mutex_unlock(&log->mutex);  
  
    return ret;  
}  


static ssize_t do_read_log_to_user(struct logger_log *log,  
                   struct logger_reader *reader,  
                   char __user *buf,  
                   size_t count)  
{  
    size_t len;  
  
    /* 
     * We read from the log in two disjoint operations. First, we read from 
     * the current read head offset up to 'count' bytes or to the end of 
     * the log, whichever comes first. 
     */  
    len = min(count, log->size - reader->r_off);  
    if (copy_to_user(buf, log->buffer + reader->r_off, len))  
        return -EFAULT;  
  
    /* 
     * Second, we read any remaining bytes, starting back at the head of 
     * the log. 
     */  
    if (count != len)  
        if (copy_to_user(buf + len, log->buffer, count - len))  
            return -EFAULT;  
  
    reader->r_off = logger_offset(reader->r_off + count);  
  
    return count;  
}  
```

# 总结
回顾最开始的疑问,Log到底是存储在哪里呢,当然是在驱动程序Logger持有的RingBuffer中,RingBuffer是维护在内核空间中的一个环形缓冲区,一旦关机,就没有了如果需要收集Log信息存储起来,可以使用logcat [-d] -f filename [-r maxSize] [-n maxCount] 命令将其存储在一系列文件中.但是普通用户身份只能查看自己进程的log, shell可以查看所有三方应用, system, root..
