package cn.vsx.yuv;

public abstract class ObjectPool<T> {
    protected final int capacity;
    private final PooledObject<T>[] pooledObjects;
    public ObjectPool(int capacity) {
        this.capacity = capacity;
        pooledObjects = new PooledObject[capacity];
        for(int i = 0 ; i < capacity ; i++){
            pooledObjects[i] = new PooledObject<>(createObject());
        }
    }

    public synchronized T getObject(){
        for(int i = 0 ; i < capacity ; i++){
            if(!pooledObjects[i].busy){
                pooledObjects[i].busy = true;
                return pooledObjects[i].t;
            }
        }
        return null;
    }

    public synchronized void releaseObject(T t){
        for(int i = 0 ; i < capacity ; i++){
            if(pooledObjects[i].t.equals(t)){
                try {
                    releaseT(t);
                }catch (Throwable throwable){
                    throwable.printStackTrace();
                }
                pooledObjects[i].busy = false;
            }
        }
    }

    protected abstract T createObject();
    protected void releaseT(T t){

    }

    private class PooledObject<T>{
        boolean busy;
        T t;
        private PooledObject(T t){
            this.t = t;
            this.busy = false;
        }
    }
}
