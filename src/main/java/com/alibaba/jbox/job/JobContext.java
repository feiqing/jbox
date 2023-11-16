package com.alibaba.jbox.job;

import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author jifang.zjf@alibaba-inc.com (FeiQing)
 * @version 1.0
 * @since 2018-09-28 20:58:00.
 */
@Slf4j(topic = "JobFramework")
@SuppressWarnings({"rawtypes", "unchecked"})
public abstract class JobContext<R> implements Serializable {

    private static final long serialVersionUID = 6258054189501546389L;

    protected Meta meta = new Meta();

    protected JobContext(String name, JobTask[] tasks) {
        meta.name = name;
        meta.tasks = tasks;
    }

    public R next() throws Throwable {
        // 已经到达终点
        if (!(++meta.idx < meta.tasks.length)) {
            throw new IllegalStateException("tasks has already reach end !");
        }

        JobTask task = meta.tasks[meta.idx];
        String desc = String.format("%s[%s/%s]('%s')", meta.name, meta.idx, meta.tasks.length, task.desc(this));

        try {
            meta.setAttribute(desc, System.currentTimeMillis());
            return (R) task.invoke(this);
        } catch (Throwable t) {
            log.error("[TASK] {} occur exception.", desc, t);
            throw t;
        } finally {
            long total = System.currentTimeMillis() - (long) meta.removeAttribute(desc);
            if (meta.traceExit && log.isInfoEnabled()) {
                log.info("[TASK] {} cost '{}'.", desc, total - meta.cost);
            }
            meta.cost = total;
        }
    }

    public static class Meta implements Serializable {

        private static final long serialVersionUID = -6456235387227434364L;

        public String name;

        public JobTask[] tasks;

        public int idx = -1;

        public long cost = 0;

        public boolean traceExit = true;

        private Map<String, Object> attributes;

        public Map<String, Object> getAttributes() {
            if (this.attributes == null) {
                this.attributes = new HashMap<>();
            }
            return this.attributes;
        }

        public Object getAttribute(String key) {
            return getAttributes().get(key);
        }

        public Object removeAttribute(String key) {
            if (attributes == null) {
                return null;
            }

            return attributes.remove(key);
        }

        public void setAttribute(String key, Object value) {
            getAttributes().put(key, value);
        }
    }
}