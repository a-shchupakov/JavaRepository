package main;

import perfomance.ICommandPacket;

public class SimpleCommandPacket implements ICommandPacket {
    public int id;

    private SimpleCommandPacket(){ }

    public SimpleCommandPacket(int id){
        this.id = id;
    }
}
