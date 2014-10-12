package com.lzw.talk.service;

import android.content.Context;
import com.avos.avoscloud.AVMessage;
import com.avos.avoscloud.AVMessageReceiver;
import com.avos.avoscloud.Session;
import com.lzw.talk.util.Logger;

import java.util.*;

/**
 * Created by lzw on 14-8-7.
 */
public class MsgReceiver extends AVMessageReceiver {
  private final Queue<String> failedMessage = new LinkedList<String>();
  public static StatusListener statusListener;
  public static Set<String> onlines = new HashSet<String>();
  public static MessageListeners messageListeners = new MessageListeners();

  @Override
  public void onSessionOpen(Context context, Session session) {
    Logger.d("onSessionOpen");
  }

  @Override
  public void onSessionPaused(Context context, Session session) {
    Logger.d("onSessionPaused");
  }

  @Override
  public void onSessionResumed(Context context, Session session) {
    Logger.d("onSessionResumed");
    while (!failedMessage.isEmpty()) {
      String msg = failedMessage.poll();
      session.sendMessage(msg, session.getAllPeers(), false);
    }
  }

  @Override
  public void onMessage(final Context context, Session session, AVMessage avMsg) {
    Logger.d("onMessage");
    ChatService.onMessage(context, avMsg, messageListeners, null);
  }

  @Override
  public void onMessageSent(Context context, Session session, AVMessage avMsg) {
    Logger.d("onMessageSent " + avMsg.getToPeerIds());
    ChatService.onMessageSent(avMsg, messageListeners, null);
  }

  @Override
  public void onMessageFailure(Context context, Session session, AVMessage avMsg) {
    ChatService.updateStatusToFailed(avMsg, messageListeners);
  }

  @Override
  public void onStatusOnline(Context context, Session session, List<String> strings) {
    Logger.d("onStatusOnline " + strings);
    onlines.addAll(strings);
    if (statusListener != null) {
      statusListener.onStatusOnline(new ArrayList<String>(onlines));
    }
  }

  @Override
  public void onStatusOffline(Context context, Session session, List<String> strings) {
    Logger.d("onStatusOff " + strings);
    onlines.removeAll(strings);
    if (statusListener != null) {
      statusListener.onStatusOnline(new ArrayList<String>(onlines));
    }
  }

  @Override
  public void onError(Context context, Session session, Throwable throwable) {
    throwable.printStackTrace();
    ChatService.onMessageError(throwable, messageListeners);
  }

  public static void registerStatusListener(StatusListener listener) {
    statusListener = listener;
  }

  public static void unregisterSatutsListener() {
    statusListener = null;
  }

  public static List<String> getOnlines() {
    return new ArrayList<String>(onlines);
  }
}