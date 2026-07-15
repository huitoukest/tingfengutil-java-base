package com.tingfeng.util.java.base.file.strategy;

/**
 * 统一进度回调接口
 * 用于文件拷贝等长时间操作的进度通知
 *
 * 设计原则：
 * 1. 进度更新传递已拷贝字节数和总字节数
 * 2. 完成时传递总字节数
 * 3. 错误时传递异常信息
 */
public interface ProgressCallback {

    /**
     * 进度更新
     * @param copiedBytes 已拷贝的字节数
     * @param totalBytes 总字节数
     */
    void onProgress(long copiedBytes, long totalBytes);

    /**
     * 操作完成
     * @param totalBytes 总字节数（实际拷贝的字节数）
     */
    void onComplete(long totalBytes);

    /**
     * 操作失败
     * @param e 异常信息
     */
    void onError(Throwable e);

    /**
     * 空实现（用于不需要回调的场景）
     */
    ProgressCallback NONE = new ProgressCallback() {
        @Override
        public void onProgress(long copiedBytes, long totalBytes) {
            // do nothing
        }

        @Override
        public void onComplete(long totalBytes) {
            // do nothing
        }

        @Override
        public void onError(Throwable e) {
            // do nothing
        }
    };
}
