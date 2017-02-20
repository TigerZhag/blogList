# 用法

# makefile格式
## 基本结构
make的主要工作是读进一个文本文件，makefile，这个文件里主要是有关哪些文件（‘target’目的文件）是从哪些别的文件（‘dependencies’依靠文件）中产生的，用什么命令来进行 这个产生过程。有了这些信息， make 会检查磁碟上的文件，如果目的文件的时间戳（该文件生成或被改动时的时间）比至少它的一 个依靠文件旧的话， make 就执行相应的命令，以便更新目的文件。（目的文件不一定是最后的可执行档，它可以是任何一个文件。） makefile 一般被叫做“makefile”或“Makefile”。当然你可以 在 make 的命令行指定别的文件名。如果你不特别指定，它会寻 找“makefile”或“Makefile”，因此使用这两个名字是最简单 的。
一个makefile包含一系列的规则，如下：

target: dependencies1 dependencies2
    action1
    action2
    .
    .
    .
make会递归检查target文件的时间戳是否比 dependencies文件的时间戳更早， 如果是，说明依赖的文件有修改，目的文件也需要修改，再依次更新目的文件。

## 规则
> tips: 隐含规则
最明显的编写规则的方法，就是一个一个检查源码文件，把它们的目标文件作为目的，源码文件作为依赖，一个一个规则写，这样如果文件比较多，就头大了，如果是C语言工程的话，编译器可以帮忙，使用 gcc -M 参数，就会自动生成规则，-MM参数则会自动剔除角括号包括的header档，因为系统header档一般是不会更新的。

## 变量
> tips: 大小写敏感
> := 和 = 需要知道什么时候应该用哪个，:= 会自动展开后面的值，而 = 不展开，即 A = test， B = $(A), C := $(A), 如果A的值变了，B会跟着变，C不会变

变量定义：
SOURCE := foo.c bar.o

变量使用：
$(SOURCE)

三个固定变量：
$@ : 当前规则的目的文件名
mce_markerlt : 当前规则的第一个依赖文件名
$^ : 当前规则的依赖文件列表

## 假象目的
假设你的一个项目最后需要产生两个可执行文件。你的主要目标 是产生两个可执行文件，但这两个文件是相互独立的——如果一个文件需要重建，并不影响另一个。你可以使用“假象目的”来 达到这种效果。一个假象目的跟一个正常的目的几乎是一样的，只是这个目的文件是不存在的。因此， make 总是会假设它需要 被生成，当把它的依赖文件更新后，就会执行它的规则里的命令 行。
如果在我们的 makefile 开始处输入：

all : exec1 exec2

其中 exec1 和 exec2 是我们做为目的的两个可执行文件。 make 把这个 'all' 做为它的主要目的，每次执行时都会尝试把 'all' 更新。但既然这行规则里没有哪个命令来作用在一个叫 'all' 的 实际文件（事实上 all 并不会在磁碟上实际产生），所以这个规则并不真的改变 'all' 的状态。可既然这个文件并不存在，所以 make 会尝试更新 all 规则，因此就检查它的依靠 exec1, exec2 是否需要更新，如果需要，就把它们更新，从而达到我们的目的。
假象目的也可以用来描述一组非预设的动作。例如，你想把所有由 make 产生的文件删除，你可以在 makefile 里设立这样一个规则：

veryclean :
rm *.o
rm myprog

前提是没有其它的规则依靠这个 'veryclean' 目的，它将永远 不会被执行。但是，如果你明确的使用命令 'make veryclean' ， make 会把这个目的做为它的主要目标，执行那些 rm 命令。
如果你的磁碟上存在一个叫 veryclean 文件，会发生什么事？这 时因为在这个规则里没有任何依靠文件，所以这个目的文件一定是最新的了（所有的依靠文件都已经是最新的了），所以既使用户明 确命令 make 重新产生它，也不会有任何事情发生。解决方法是标明所有的假象目的（用 .PHONY），这就告诉 make 不用检查它们 是否存在于磁碟上，也不用查找任何隐含规则，直接假设指定的目的需要被更新。在 makefile 里加入下面这行包含上面规则的规则：

.PHONY : veryclean

就可以了。注意，这是一个特殊的 make 规则，make 知道 .PHONY 是一个特殊目的，当然你可以在它的依靠里加入你想用的任何假象 目的，而 make 知道它们都是假象目的。

## 函数
makefile 里的函数跟它的变量很相似——使用的时候，你用一个 $ 符号跟开括号，函数名，空格后跟一列由逗号分隔的参数，最后用关括号结束。例如，在 GNU Make 里有一个叫 'wildcard' 的函 数，它有一个参数，功能是展开成一列所有符合由其参数描述的文件名，文件间以空格间隔。你可以像下面所示使用这个命令：

SOURCES = $(wildcard *.c)

这行会产生一个所有以 '.c' 结尾的文件的列表，然后存入变量 SOURCES 里。当然你不需要一定要把结果存入一个变量。
另一个有用的函数是 patsubst （ patten substitude, 匹配替换的缩写）函数。它需要３个参数——第一个是一个需要匹配的式样，第二个表示用什么来替换它，第三个是一个需要被处理的由空格分隔的字列。例如，处理那个经过上面定义后的变量，

OBJS = $(patsubst %.c,%.o,$(SOURCES))

这行将处理所有在 SOURCES 字列中的字（一列文件名），如果它的 结尾是 '.c' ，就用 '.o' 把 '.c' 取代。注意这里的 % 符号将匹 配一个或多个字符，而它每次所匹配的字串叫做一个‘柄’(stem) 。 在第二个参数里， % 被解读成用第一参数所匹配的那个柄。

# 一个有效的makefile

```makefile
######################################
#
# Generic makefile
#
# by George Foot
# email: george.foot@merton.ox.ac.uk
#
# Copyright (c) 1997 George Foot
# All rights reserved.
# 保留所有版权
#
# No warranty, no liability;
# you use this at your own risk.
# 没保险，不负责
# 你要用这个，你自己担风险
#
# You are free to modify and
# distribute this without giving
# credit to the original author.
# 你可以随便更改和散发这个文件
# 而不需要给原作者什么荣誉。
# （你好意思？）
#
######################################

### Customising
# 用户设定
#
# Adjust the following if necessary; EXECUTABLE is the target
# executable's filename, and LIBS is a list of libraries to link in
# (e.g. alleg, stdcx, iostr, etc). You can override these on make's
# command line of course, if you prefer to do it that way.
#
# 如果需要，调整下面的东西。 EXECUTABLE 是目标的可执行文件名， LIBS
# 是一个需要连接的程序包列表（例如 alleg, stdcx, iostr 等等）。当然你
# 可以在 make 的命令行覆盖它们，你愿意就没问题。
#

EXECUTABLE := mushroom.exe
LIBS := alleg

# Now alter any implicit rules' variables if you like, e.g.:
#
# 现在来改变任何你想改动的隐含规则中的变量，例如

CFLAGS := -g -Wall -O3 -m486
CXXFLAGS := $(CFLAGS)

# The next bit checks to see whether rm is in your djgpp bin
# directory; if not it uses del instead, but this can cause (harmless)
# `File not found' error messages. If you are not using DOS at all,
# set the variable to something which will unquestioningly remove
# files.
#
# 下面先检查你的 djgpp 命令目录下有没有 rm 命令，如果没有，我们使用
# del 命令来代替，但有可能给我们 'File not found' 这个错误信息，这没
# 什么大碍。如果你不是用 DOS ，把它设定成一个删文件而不废话的命令。
# （其实这一步在 UNIX 类的系统上是多余的，只是方便 DOS 用户。 UNIX
# 用户可以删除这５行命令。）

ifneq ($(wildcard $(DJDIR)/bin/rm.exe),)
RM-F := rm -f
else
RM-F := del
endif

# You shouldn't need to change anything below this point.
#
# 从这里开始，你应该不需要改动任何东西。（我是不太相信，太ＮＢ了！）

SOURCE := $(wildcard *.c) $(wildcard *.cc)
OBJS := $(patsubst %.c,%.o,$(patsubst %.cc,%.o,$(SOURCE)))
DEPS := $(patsubst %.o,%.d,$(OBJS))
MISSING_DEPS := $(filter-out $(wildcard $(DEPS)),$(DEPS))
MISSING_DEPS_SOURCES := $(wildcard $(patsubst %.d,%.c,$(MISSING_DEPS)) \
$(patsubst %.d,%.cc,$(MISSING_DEPS)))
CPPFLAGS += -MD

.PHONY : everything deps objs clean veryclean rebuild

everything : $(EXECUTABLE)

deps : $(DEPS)

objs : $(OBJS)

clean :
@$(RM-F) *.o
@$(RM-F) *.d

veryclean: clean
@$(RM-F) $(EXECUTABLE)
```
