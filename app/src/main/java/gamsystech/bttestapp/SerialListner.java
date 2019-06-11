package gamsystech.bttestapp;

public interface SerialListner
{
    void onSerialConnect();
    void onSerialConnectError (Exception e);
    void onSerialRead(byte[] data);
    void onSerialIoError(Exception e);
}
