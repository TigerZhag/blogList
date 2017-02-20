# 背景
 本来的SSD上装得Windows 10, 新买了一个mSATA接口的SSD，打算装一个Ubuntu
# 安装盘制作
 [下载Ubuntu系统镜像](https://www.ubuntu.com/download/alternative-downloads)，!(我使用的是Universal USB Installer)[https://www.pendrivelinux.com/universal-usb-installer-easy-as-1-2-3/]， windows下的制作工具。
# 注意事项
1. 安装照常安装，网上教程一抓一大把，唯一值得注意的是引导， 由于是在两个硬盘中的系统，如果引导是在两个硬盘中，就只能切换bios的boot order才能切换系统了，所以Ubuntu的 /boot 分区需要挂载在windows系统所在的SSD中，幸好本来那块SSD留了大概1个G没有分区，正好分了200M给 /boot，分区策略见仁见智，其实自己用的硬盘容量现在都够大，没有必要太过纠结，安装完毕后使用easyBCD添加一个Ubuntu的引导项即可。
