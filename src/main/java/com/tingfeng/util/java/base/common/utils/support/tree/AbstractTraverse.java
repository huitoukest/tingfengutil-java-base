package com.tingfeng.util.java.base.common.utils.support.tree;

public abstract class AbstractTraverse implements Traverse{
    /**
     * 遍历中是 List 的正序还是逆序
     * true = 逆序 ; false = 正序;
     */
    private boolean isReverse;

    public AbstractTraverse(boolean isReverse){
        this.isReverse = isReverse;
    }

    public boolean isReverse() {
        return isReverse;
    }

    public void setReverse(boolean reverse) {
        isReverse = reverse;
    }
}
