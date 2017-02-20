# 关于root精灵
1. 获取root权限后自动下载kingroot作为权限管理, 并安装到入/system/app/目录下,添加 iaA 权限
2. 将su,ku.sud,daemonsu等文件放入 /system/xbin/目录下,并添加 iaA 权限
3. 开启两个ku.sud进程相互监听, 一方被关闭另一方就重新打开之
4. ku.sub进程被kill一个之后,终端很多命令就无法执行了,不知道怎么做到的.

# 如何删除
1. 以下内容需在adb shell中执行或者下载Android终端模拟器
2. 前提得有busybox, 因为chattr命令Android中没有
3. 切换至root用户
4. 修改kingroot.apk权限, 删除kingroot.apk:
 busybox chattr -iaA /system/app/Kingroot.apk
 rm /system/app/Kingroot.apk
5. 修改/system/xbin/目录下su相关文件权限,并删除
  busybox chattr -iaA /system/xbin/*su*
  rm /system/xbin/*su*
*6. 杀死一个ku.sud进程
 kill [pid]
7 麻溜的重启,不然ku.sud进程会再次生成/system/xbin/下那几个文件


