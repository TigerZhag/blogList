## 概述
> [这里](http://coolshell.cn//wp-content/uploads/2010/10/rijndael_ingles2004.swf)是波士顿大学的Howard Straubing做的一段很直白的动画。

AES加密过程是在一个4×4的字节矩阵上运作，这个矩阵又称为“体（state）”，其初值就是一个明文区块（矩阵中一个
元素大小就是明文区块中的一个Byte）。（Rijndael加密法因支持更大的区块，其矩阵行数可视情况增加）加密时，
各轮AES加密循环（除最后一轮外）均包含4个步骤：
1.	AddRoundKey—矩阵中的每一个字节都与该次回合密钥（round key）做XOR运算；每个子密钥由密钥生成方案产生。
2.	SubBytes—通过一个非线性的替换函数，用查找表的方式把每个字节替换成对应的字节。
3.	ShiftRows—将矩阵中的每个横列进行循环式移位。
4.	MixColumns—为了充分混合矩阵中各个直行的操作。这个步骤使用线性转换来混合每内联的四个字节。最后一个加密循环中省略MixColumns步骤，而以另一个AddRoundKey替换。
表2.2 AES密钥长度与轮数的关系

| | | | |
|:------|:-------|:--------|:-------|
|密钥长度|	128	|192	|256|
|圈数|	10|	12	|14|
 
![AES算法流程图.png](http://upload-images.jianshu.io/upload_images/1448134-e5216ffc5431acb6.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

## S盒变换（SubBytes）
![AES中的S盒.png](http://upload-images.jianshu.io/upload_images/1448134-8014f31facd46aca.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

矩阵中的各字节通过一个8位的S-box进行转换。这个步骤提供了加密法非线性的变换能力。S-box与GF（28）上的乘法反元素有关，已知具有良好的非线性特性。为了避免简单代数性质的攻击，S-box结合了乘法反元素及一个可逆的仿射变换矩阵建构而成。此外在建构S-box时，刻意避开了固定点与反固定点，即以S-box替换字节的结果会相当于错排的结果。AES算法中的S盒如图2.2所示
例如一个字节为0x19，经过S盒变换查找n(1,9) = 0xd4,所以就替换为0xd4。
## 行变换 （ShiftRows）
ShiftRows描述矩阵的行操作。在此步骤中，每一行都向左循环位移某偏移量。在AES中（区块大小128位），第一行维持不变，第二行里的每个字节都向左循环移动一格。同理，第三行及第四行向左循环位移的偏移量就分别是2和3。经过ShiftRows之后，矩阵中每一竖列，都是由输入矩阵中的每个不同列中的元素组成。
## 列变换 （MixColumns）
在MixColumns步骤，每一列的四个字节通过线性变换互相结合。每一列的四个元素分别当作 的系数，合并即为GF（28）中的一个多项式，接着将此多项式和一个固定的多项式在modulo  下相乘。此步骤亦可视为Rijndael有限域之下的矩阵乘法。MixColumns函数接受4个字节的输入，输出4个字节，每一个输入的字节都会对输出的四个字节造成影响。因此ShiftRows和MixColumns两步骤为这个密码系统提供了扩散性。
## 扩展密钥（AddRoundKey）
AES算法利用外部输入密钥K(密钥串的字数为Nk),通过密钥的扩展程序得到共计4(Nr+1)字的扩展密钥。它涉及如下三个模块:
(1)位置变换(rotword)——把一个4字节的序列[A,B,C,D]变化成[B,C,D,A]；
(2)S盒变换(subword)——对一个4字节进行S盒代替；
(3)变换Rcon[i]——Rcon[i]表示32位比特字[xi-1,00,00,00]。这里的x是（02），如 Rcon[1]=[01000000]；Rcon[2]=[02000000]；Rcon[3]=[04000000]…… 　　 
扩展密钥的生成：扩展密钥的前Nk个字就是外部密钥K；以后的字W[[ｉ]]等于它前一个字W[[i-1]]与前第Nk个字W[[i-Nk]]的“异或”,即W[[ｉ]]=W[[i-1]]W[[ｉ- Nk]]。但是若ｉ为Nk的倍数,则W[ｉ]=W[i-Nk]Subword(Rotword(W[[ｉ-1]]))Rcon[i/Nk]。