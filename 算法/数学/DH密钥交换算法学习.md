# what's Diffie–Hellman key exchange
> 维基定义: 是一种安全协议。它可以让双方在完全没有对方任何预先信息的条件下通过不安全信道创建起一个密钥。这个密钥可以在后续的通讯中作为对称密钥来加密通讯内容

简而言之，在双方都不知道对方的加密密钥的情况下进行通信，这时就会遇到一个麻烦，收到信息后如何解密，DH正
是为了解决这个问题而生。
## 应用场景

# why should use DH

# how to use DH 
## 基础知识
### 欧拉函数：
在数论，对正整数n，欧拉函数是小于n的数中与n互质的数的数目  
![欧拉函数推导公式](http://upload-images.jianshu.io/upload_images/1448134-a70ea08422de4a23.jpg?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)  
特别的，若n是质数p的k次幂，![](http://upload-images.jianshu.io/upload_images/1448134-fea9182b53a7daab.jpg?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240) ，因为除了p的倍数外，其他数都跟n互质。

### 原数：

### 实现