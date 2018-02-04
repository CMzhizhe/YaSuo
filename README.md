# 实现打开相册并压缩,返回压缩后图片路径

![img](https://github.com/caocao123/YaSuo/blob/master/gif%E5%9B%BE%E7%89%87/%E5%9B%BE%E7%89%87%E5%8E%8B%E7%BC%A9%E5%A4%84%E7%90%86.gif)

# 代码使用方式

```js
 var picAiCompressMoudle  = api.require('picaicompress');
   var param = {showCamera:true,single:false,requestNum:9,comProgress:true,targetSize:500};
        picAiCompressMoudle.openAiBum(param,function(ret,err){
            console.log("状态码:"+ret.status);
            if(ret.picPaths){
                console.log("压缩后图片路径:"+ret.picPaths);
            }
        });
  showCamera:是否显示相机
  single:是否单选
  requestNum:需要选择的数量
  comProgress:是否压缩
  targetSize:压缩后的目标大小  最后压缩的结果是<=targetSize
  
    ret.status:
           返回1 表示压缩完成
           返回2 表示正在压缩中
    err.status:
           返回0 表示失败
    err.errorMessage:
           错误的信息提示
 
   压缩后图片获取，返回数组，以“,”分割
  
```

# 注意
你需要在你的项目中加入权限
```
  <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
  <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
```



使用了第三方的打开相册技术：https://github.com/lovetuzitong/MultiImageSelector
