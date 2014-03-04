package com.example.socketexample.socket;
import com.example.socketexample.socket.IRemoteServiceCallBack;
interface IRemoteService{
    void registerCallBack(IRemoteServiceCallBack cb);
    void unregisterCallBack(IRemoteServiceCallBack cb);
    void sendMessage(String message);
    void startServer();
    void stopServer();

}