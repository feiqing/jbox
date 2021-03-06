package com.github.jbox.rpc.akka.impl;

import akka.actor.UntypedActor;
import akka.remote.AssociatedEvent;
import akka.remote.AssociationErrorEvent;
import akka.remote.DisassociatedEvent;
import lombok.extern.slf4j.Slf4j;

/**
 * @author jifang.zjf@alibaba-inc.com (FeiQing)
 * @version 1.0
 * @since 2019/10/8 2:21 AM.
 */
@Slf4j(topic = "JboxRpcClient")
public class EventMonitorActor extends UntypedActor {

    @Override
    public void onReceive(Object message) {
        if (message instanceof DisassociatedEvent) {
            String host = ((DisassociatedEvent) message).getRemoteAddress().host().get();
            Configs.putFailed(host, message.toString());
        } else if (message instanceof AssociationErrorEvent) {
            String host = ((AssociationErrorEvent) message).getRemoteAddress().host().get();
            Configs.putFailed(host, message.toString());
        } else if (message instanceof AssociatedEvent) {
            String host = ((AssociatedEvent) message).getRemoteAddress().host().get();
            Configs.removeFailed(host);
        }

        // todo: detail
        log.warn("Event: {}", message);
    }
}
