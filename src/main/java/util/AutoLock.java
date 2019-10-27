package util;

import java.util.concurrent.locks.Lock;

public class AutoLock implements AutoCloseable{
    protected Lock lock;

    public static AutoLock lock(Lock lock){
        return new AutoLockImp(lock);
    }

    public static AutoLock lockInterruptibly(Lock lock) throws InterruptedException{
        return new AutoLockInterruptImp(lock);
    }

    public AutoLock(Lock lock){
        this.lock=lock;
    }

    @Override
    public void close(){
        try{
            lock.unlock();
        }catch(IllegalMonitorStateException ignored){
        }
    }

    private static class AutoLockImp extends AutoLock{

        public AutoLockImp(Lock lock){
            super(lock);
            lock.lock();
        }
    }

    private static class AutoLockInterruptImp extends AutoLock{

        public AutoLockInterruptImp(Lock lock) throws InterruptedException{
            super(lock);
            lock.lockInterruptibly();
        }
    }
}

