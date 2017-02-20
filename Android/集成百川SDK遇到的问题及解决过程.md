## 背景
由于项目中需要拍摄小视频，找到了趣拍的SDK，而且发现阿里百川中集成了众多开发必需的SDK，
于是就集成了百川下的 高德地图、IM、友盟等SDK。由于百川中没有趣拍的选项，所以需要把两
个SDK合并起来，期间花了两三天功夫才把所有问题解决完，趁今晚把问题总结一下
##问题总结  
由于我是问题解决完了才总结的，期间很多问题都忘了，翻自己上网历史记录才找到的，所以很多都忘了，只能想起来再加。另外由于需要的包比较多，所以遇到的问题有些比较奇葩。。。IDE为Android Studio 1.5正式版

 - [Qupaisdk], the android stack error message is Fail to start the plugin, 
which is caused by String array resource ID #0x7f0d0000  
这个问题我是因为自己的SDK中V7兼容包是23.1.1的，改成22.2.0就好了，版本太高可能不兼容。  

 - resource xxx.xxx.xxx:attr/xxx not found  
这是因为少导包了，仔细查看自己V7或者V4包有没有，另外下载下来的SDK里，趣拍中的V13个人感觉是
没什么用的，因为V13是为了Android 3.2及更高版本开发使用，一般只有开发平板应用中要用到。  

 
 - finished with non-zero exit value 1
 这个问题很多原因都会导致，平常忘了是怎么解决了，但是这次是在build.gradle中添加了
 

```
	dexOptions {
        javaMaxHeapSize "4g"
    }
```
		然后就好了
	
 - finished with nin-zero exit value 2
 这个毫无疑问就是jar包重复，仔细找特别是V4和V7的兼容包，看看各个Module的依赖关系，就能解决了。

 - finished with nin-zero exit value 3
 头次碰到exit value 3，clean一下，rebuild一下，又好了，所以项目做了什么大的变动，最好还是rebuild一下。
 

 - app:clean FAILED Error:Execution failed for task ':app:clean'.Unable to delete file: 
 clean的时候有时候删除不掉原来生成的文件，猜测可能是权限或者是正在占用的原因，不管了重启一下Android studio，再clean，好了。。
 
 - ![这里写图片描述](http://img.blog.csdn.net/20151205235844930)
 项目中用到了ViewPager，本来是好好的，添加上OneSDK的依赖后突然出问题了，发现OneSDK中有一个V4的jar包，就把它给删了，换成在OneSDK的build.gradle中compile V7兼容包，这样会自动依赖V4，并且也不会由于自己也要用V7而造成冲突。
 
 - ![这里写图片描述](http://img.blog.csdn.net/20151206000640614)
 这一个是因为我为了减少代码量使用了java8，很显然不兼容，换成java7就好了，修改一大堆lambda表达式真蛋疼。。
 - ![这里写图片描述](http://img.blog.csdn.net/20151206001123309)
  很显然，方法数超过65K了，这里推荐两个方法数统计工具：
 1.[dexcount-gradle-plugin](https://github.com/KeepSafe/dexcount-gradle-plugin),使用方法链接里有，自己看。
 2.[Android Methodscount](http://www.methodscount.com/) 这个是个AndroidStudio插件，可以在Setting -> plugin->搜索Android methods count，安装即可。
  解决方法：第一步：在自己项目的build.gradle中android{defaultConfig{}}中添加：multiDexEnabled true。如果添加这个，会遇到下面这个问题
  
 - FATAL EXCEPTION: java.lang.NoClassDefFoundError: android.support.v7.appcompat.R$layout
 之类的明明编译时可以找到但是打了包就找不到的资源，这是因为dex文件拆分成了两个，Lollipop（5.0）以上可以自动找到资源，Lollipop以下需要我们在Application中指明:
 首先在build.gradle中：

```
 dependencies {
compile 'com.android.support:multidex:1.0.0'
}
defaultConfig {          
multiDexEnabled true
}
```

然后再Manifest.xml文件中

```
<application
        ...
        android:name="android.support.multidex.MultiDexApplication">
        ...
</application>
```

或者如果你写了Application类，可以这样：

```
 import android.support.multidex.MultiDexApplication;
 import android.support.multidex.MultiDex;


 public class MyApplication extends MultiDexApplication {

 // ......

    @Override
    protected void attachBaseContext(Context base) {
    super.attachBaseContext(base);
    MultiDex.install(this);
    }
}
```

 - 最后，高德地图也就是Amap初始化不成功，本想着百川会自动验证key的，没想到还是自己写的，先在百川SDK下载那里看看自己的Key，然后在OneSDK的Manifest.xml中添加
 

```
		<!-- 高德Key -->
        <meta-data
            android:name="com.amap.api.v2.apikey"
            android:value="你的key"/>
```

最后，合并成功！