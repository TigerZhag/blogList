##standard
- 标准模式，每次启动一个Activity就重新创建一个新的实例。每个实例也可以属于不同的
任务栈。  
- 谁启动了这个Activity，这个Activity就运行在启动他的那个Activity所在的栈中。
- 如果使用ApplicationContext去启动standard模式的Activity会报错，因为非Activity类型的
Context并没有所谓的任务栈。解决方法是为待启动的Activity指定FLAG_ACTIVITY_NEW_TASK
标记位，这样启动时就会创建一个新的任务栈。这时启动实际是singleTask模式启动的。

##singleTop
- 栈顶复用模式，如果新Activity已经位于任务栈的栈顶，那么此Activity不会重新创建，只调用
onNewIntent()方法。OnCreate()ONStart()方法并没有被调用。
- 如果新Activity没有在栈顶，仍然会重新创建。

##singleTask
- 栈内复用模式。单实例，只要Activity在一个栈中存在，那么多次启动都不会重新创建实例
，只调用OnNewIntent()方法，并且clearTop，
- 栗子Activity A，启动后先寻找是否存在A想要的任务栈（TaskAffinity），不存在就创建
一个把A放进去，存在的话 有实例？调到栈顶并onNewIntent() : 创建A并压入栈。

##singleInstance
- 单实例模式。加强的singleTask模式，只能单独地位于一个任务栈中。

##tips:
- 假设目前两个任务栈如图，C、D都为SingleTask模式，前台为A、B所在任务栈。现在请求启动D，
那么整个后台任务栈都会被切换到前台。
![这里写图片描述](file:///C:/Users/Administrator/Desktop/singleTask.png)

- 在singleTask启动模式中，提到过某个Activity所需的任务栈，跟一个参数有关。
TaskAffinity，这个参数表示了一个Activity所需要的任务栈的名字，默认为应用的包名。
也可以为每个Activity指定。

- TaskAffinity属性主要和SingleTask启动模式或者allowTaskReparenting属性配对使用，其他情况没有意义。

- 当taskAffinity和allowTaskReparenting结合的时候，当应用A启动了应用B的某个Activity C，C是默认模式
所以运行在A的任务栈中，然后启动应用B，C发现有了和自己的Affinity名字相同的任务栈，就会转移过去了。