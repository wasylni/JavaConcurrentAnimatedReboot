package com.vgrazi.jca.sprites;

import com.vgrazi.jca.states.ThreadState;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;

import java.awt.*;

/**
 * A ThreadSprite represents one thread, and retains all of the state related to that thread,
 * including the Java thread itself, the shape, xPosition, and the action, which is called by slide,
 * and is used to change the state
 * Note: We should really create the thread in the constructor, but its Runnable needs access to this class's
 * running flag. So construct the sprite, then add the Runnable.
 */
public class ThreadSprite<S> extends Sprite<S> implements InitializingBean  {

    protected Thread thread;
    @Value("${arrow-length}")
    protected int arrowLength;
    /**
     * set to true to have the sprite animate from right to left when failed
     */
    private boolean retreating;

    public Thread getThread() {
        return thread;
    }

    public void setThread(Thread thread) {
        this.thread = thread;
    }

    @Value("${pixels-per-y-step}")
    protected int height;

    public Thread.State getThreadState() {
        Thread.State state = null;
        if (thread != null) {
            state = thread.getState();
        }
        return state;
    }
    /**
     * Create the thread associated with this runnable, and starts it
     */
    public void attachAndStartRunnable(Runnable runnable) {
        thread = new Thread(runnable);
        thread.start();
    }

    public void setNextXPosition() {
        getState().advancePosition(this);
    }

    @Override
    public void render(Graphics2D graphics) {
        Color color = getThreadContext().getColor(this);
        graphics.setColor(color);
        graphics.drawLine(getXPosition() - arrowLength, getYPosition(), getXPosition(), getYPosition());
        drawThreadCap(graphics);
        renderMessage(graphics);
    }

    @Override
    public void renderMessage(Graphics2D graphics) {
        if(thread != null && getThreadContext().isDisplayThreadNames()) {
            setMessage(thread.getName());
        }
        super.renderMessage(graphics);
    }

    /**
     * Draws the ball (or whatever) at the end of the thread line
     */
    protected void drawThreadCap(Graphics2D graphics) {
        graphics.setColor(getThreadContext().getColorByInstance(this));
        int offset = isRetreating() && getDirection() == Direction.left ? arrowLength:0;
        graphics.fillOval(getXPosition() -8 -offset, getYPosition()-5, 10, 10);
    }

    /**
     * Returns our internal thread state, reflecting the native thread state, with some adjustments (new and runnable
     * are both considered runnable, and waiting and timed-waiting are both considered waiting.
     */
    public ThreadState getState() {
        if(thread == null) {
            return null;
        }
        if(isRetreating()) {
            return getThreadContext().retreating;
        }
        if(this instanceof PooledThreadSprite) {
            return getThreadContext().pooled;
        }
        switch (thread.getState()) {
            case NEW:
            case RUNNABLE:
                return getThreadContext().runnable;
            case WAITING:
            case TIMED_WAITING:
                return getThreadContext().waiting;
            case BLOCKED:
                return getThreadContext().blocked;
            case TERMINATED:
                return getThreadContext().terminated;
            default:
                throw new IllegalArgumentException("Unknown thread state " + thread.getState());
        }
    }

    @Override
    public void afterPropertiesSet() {
        setYPosition(getNextYPositionFromContext());
    }

    protected int getNextYPositionFromContext() {
        return getThreadContext().getNextYPosition(height);
    }

    @Override
    public String toString() {
        return "ThreadSprite{" +
                "ID=" + getID() +
                ", state=" + getState() +
                ", native-state=" + thread.getState() +
//                ", x-position=" + getXPosition() +
                ", y-position=" + getYPosition() +
                ", relative_position=" + getRelativePosition() +
                ", " + super.toString() +
                '}';
    }

    public boolean isRetreating() {
        return retreating;
    }

    public void setRetreating(boolean retreating) {
        this.retreating = retreating;
    }
}
