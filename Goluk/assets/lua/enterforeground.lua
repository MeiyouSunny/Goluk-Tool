--应用程序平台控制，当应用程序进入后台时调用
--此处主要是因为ios平台在进入后台之后，所有线程事件等都处于睡眠状态，由此引起一些如socket等接口出现问题

require"lua/socket"
--socket环境恢复
tiros.socket.socketEnvResume();

