BEGIN {

############################################### Value to be set manually ###############################################
    # Robotic  Discovery
    # Robotic  DISCOVERY;RoboId;RoboDisplayID;AttachedToDeviceID;AttachedToDeviceType;RoboticType;Vendor;Model;SerialNumber;AdditionalInformation
    print "DISCOVERY;1;Robot 1;1;Enclosure;Cassette Robot;DELL;Robot Rock 5;SN12454EE;Firmware Version: 10024114"
    print "DISCOVERY;2;Robot 2;1;Enclosure;Cassette Robot;DELL;Robot Rock 5;SN12454EE;Firmware Version: 10024114_0"
    print "DISCOVERY;3;Robot 3;1;Enclosure;Cassette Robot;DELL;Robot Rock 5;SN12454EE;Firmware Version: 10024114-1"

    # Robotic  Collect
    # Robotic  COLLECT;RoboId;RoboStatus;RobotStatusInformation;MoveCount;ErrorCount
    print "COLLECT;1;ok;;50;2"
    print "COLLECT;2;failed;Cassette blocked;250;30"
    print "COLLECT;3;ok;;1;10;"
}