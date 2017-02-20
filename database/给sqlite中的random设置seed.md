# 0x01
 As we know, 在sqlite中random函数是不能有参数的, 也就是说不能设置seed, 每次调用都是返回一个随机的介于-9223372036854775808 and +9223372036854775807之间的整数.如:  
 ```shell
 select random();
 select * from table_name order by random();
 ```
 上面的语句可能经常用,但是random()返回的是一个随机整数, 大家有没有想过为什么 order by 一个整数, 就可以产生随机顺序呢, 如果我自己设置一个整数, 结果是什么样呢, 比如:
 ``` shell
 select * from table_name order by -5089225945763057372;
 ```
 发现结果跟没排序没什么两样, order by后面一般都是跟列名阿, 这就涉及到在sqlite中源码的实现了,找个时间看下源码, 这里就不深入讨论了,好像是生成一个伪列来实现的, 下面讨论一种自定义的简单随机方法.
# 0x02
 那么如果我们需要一个seed, 每次如果seed一样,返回的数据集顺序需要也完全一样怎么办呢, 在stackOverflow中发现了一个php的解决方案,而且解释的也很详细, 可以看下这个:[order by random with seed in sqlite](http://stackoverflow.com/questions/24256258/order-by-random-with-seed-in-sqlite).  
 在这里给出Android中的实现.
# 0x03
 分为三部
 1. 生成seed(形如 0.54534238371923827955579364758491),一般计算一个字符串的MD5值,在转换成十六进制字符串, 替换掉里面的[A,F]区间内的数就可以了;
 2. 使用 ID * seed, 并取小数位
 3. 使用这个值排序
 
 计算结果如下表所示:
 
| row_id  |  id * seed  | sort order  |
|:---:|: --:|:---:|
|1| 0.545342384 | 545342384 |
|2| 1.090684767 | 090684767 |
|3| 1.636027151 | 636027151 |
|4| 2.181369535 | 181369535 |
|5| 2.726711919 | 726711919 |
|6| 3.272054302 | 272054302 |
|7| 3.817396686 | 817396686 |
|8| 4.362739070 | 362739070 |

经过按照sort order排序之后, 就成这样子啦, 

| row_id  |  id * seed  | sort order  |
|:---:|: --:|:---:|
|2| 1.090684767 | 090684767 |
|4| 2.181369535 | 181369535 |
|6| 3.272054302 | 272054302 |
|8| 4.362739070 | 362739070 |
|1| 0.545342384 | 545342384 |
|3| 1.636027151 | 636027151 |
|5| 2.726711919 | 726711919 |
|7| 3.817396686 | 817396686 |


不过这里数据有点少, 数据量比较大的时候, 就比较随机了,如下:

```shell
sqlite> select _id from points order by substr(_id * 0.5453423837192, length(_id) + 2) limit 10;
3242
4863
6484
1621
805
2426
4047
5668
7289
161
```

 代码如下:
 
```java
String seed = "hello world";
MessageDigest md = MessageDigest.getInstance("MD5");
byte[] seedMd5 = md.digest(seed.getBytes());
String seedHex = bytesToHex(seedMd5);
String finalSeed = "0." + num.replace('0','7').replace('a', '3').replace('b', '1').replace('c', '5').replace('d', '9').replace('e', '8').replace('f', '4');
String query = "select * from table_name order by substr(id * " + finalSeed + ",length(id) + 2);";
Cursor cursor = database.rawQuery(query);

public static String byteArrayToHex(byte[] byteArray) {
        // 首先初始化一个字符数组，用来存放每个16进制字符
        char[] hexDigits = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

        // new一个字符数组，这个就是用来组成结果字符串的（解释一下：一个byte是八位二进制，也就是2位十六进制字符（2的8次方等于16的2次方））
        char[] resultCharArray = new char[byteArray.length * 2];
        // 遍历字节数组，通过位运算（位运算效率高），转换成字符放到字符数组中去
        int index = 0;

        for (byte b : byteArray) {
            resultCharArray[index++] = hexDigits[b >>> 4 & 0xf];
            resultCharArray[index++] = hexDigits[b & 0xf];
        }
        return new String(resultCharArray);
    }
```
