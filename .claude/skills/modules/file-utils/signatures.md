# FileUtils 方法签名

## 文件读写（简洁封装）

| 方法 | 说明 |
|------|------|
| `static byte[] readFileToByteArray(File)` | 读取文件为字节数组 |
| `static String readFileToString(File)` | 读取文件为 String (UTF-8) |
| `static String readFileToString(File, Charset)` | 读取文件为 String (指定编码) |
| `static void writeByteArrayToFile(File, byte[])` | 字节数组写入文件 |
| `static void writeStringToFile(File, String)` | String 写入文件 (UTF-8) |
| `static void writeStringToFile(File, String, Charset, boolean)` | String 写入文件（指定编码+追加模式） |

## 行写入操作

| 方法 | 说明 |
|------|------|
| `static void writeLine(File, String)` | 追加一行（UTF-8，自动换行） |
| `static void writeLine(File, String, Charset)` | 追加一行（指定编码） |
| `static void writeLine(File, String, Charset, boolean)` | 写入一行（追加/覆盖） |
| `static void writeLines(File, List<String>)` | 追加多行（UTF-8） |
| `static void writeLines(File, List<String>, Charset, boolean)` | 追加多行（指定编码+模式） |

## 异步文件读写

| 方法 | 说明 |
|------|------|
| `static CompletableFuture<byte[]> readFileAsync(File, Object, BiFunction, CancellationToken)` | 异步读取文件 |
| `static CompletableFuture<Boolean> writeFileAsync(File, byte[], Object, BiFunction, CancellationToken)` | 异步写入字节数组 |
| `static CompletableFuture<Boolean> writeFileAsync(File, String, Charset, boolean, Object, BiFunction, CancellationToken)` | 异步写入字符串 |
| `static CompletableFuture<Boolean> writeLineAsync(File, String, Charset, boolean, Object, BiFunction, CancellationToken)` | 异步追加一行 |
| `static CompletableFuture<Long> copyFileAsync(File, File, Object, Function, int, CancellationToken)` | 异步文件拷贝 |

## 现有方法（已实现）

| 方法 | 说明 |
|------|------|
| `static void uploadFile(...)` | 文件上传到 HTTP URL |
| `static void downFile(...)` | 从 URL 下载文件 |
| `static boolean deleteFile(path)` | 删除文件 |
| `static void deleteFolder(File, boolean, boolean)` | 删除文件夹 |
| `static void createFolder(path)` | 创建文件夹 |
| `static void createFile(path)` | 创建文件 |
| `static void copyFile(srcPath, destPath)` | 文件拷贝（channel 方式） |
| `static void copyFileByFileChannel(...)` | 带进度的文件拷贝 |
| `static void copyDirectory(...)` | 拷贝整个目录 |
| `static void writeFile(File, OutputStream, callback)` | 用输出流写出文件 |
| `static File[] getAllFilesByAFolders(...)` | 递归获取目录下所有文件 |
| `static String getFileNoExtensionName(path)` | 获取无扩展名的文件名 |
| `static String getFileExtension(path)` | 获取文件扩展名 |
| `static String getFileNameByPath(path)` | 通过路径获取文件名 |
| `static String transFileToString(File, Base64ConvertToStringI)` | 文件转 Base64 字符串 |
