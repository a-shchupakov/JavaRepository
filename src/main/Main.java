package main;

import commands.CommandFactory;
import commands.ICommand;
import commands.ICommandPacket;
import managment.Manager;
import utils.encrypt.IEncryptor;
import utils.serializers.Serializer;
import utils.encrypt.XorEncryptor;

public class Main {

    public static void main(String[] args) {
        CommandFactory factory = new CommandFactory() {
            @Override
            public ICommand create_command(ICommandPacket packet) {
                return new ICommand() {
                    @Override
                    public void execute() {
                        SimpleCommandPacket commandPacket = (SimpleCommandPacket) packet;
                        System.out.println("Command executed which was sent by user " + commandPacket.id);
                    }
                };
            }
        };

        IEncryptor encryptor = new XorEncryptor("same super secret phrase".getBytes());

        StraightEncryptingDataTransporter transporter1 = new StraightEncryptingDataTransporter(encryptor);
        StraightEncryptingDataTransporter transporter2 = new StraightEncryptingDataTransporter(encryptor);
        transporter1.setAnotherTransporter(transporter2);
        transporter2.setAnotherTransporter(transporter1);

        Manager manager1 = new Manager(new Serializer(), transporter1, factory);
        SimpleUser user1 = new SimpleUser(12345, manager1);
        manager1.setCommandProcessor(user1);

        Manager manager2 = new Manager(new Serializer(), transporter2, factory);
        SimpleUser user2 = new SimpleUser(999, manager2);
        manager2.setCommandProcessor(user2);

        System.out.println("Finished constructing");
        ICommandPacket packet1 = user1.createPacket();
        manager1.sendToAnotherProcessor(packet1);
        manager2.getFromAnotherManager();

        ICommandPacket packet2 = user2.createPacket();
        manager2.sendToAnotherProcessor(packet2);
        manager1.getFromAnotherManager();
    }
}
