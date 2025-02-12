BEGIN {

############################################### Value to be set manually ###############################################
    # TapeDrive  Discovery
    # TapeDrive  DISCOVERY;TdId;TdDisplayId;AttachedToDeviceID;AttachedToDeviceType;Vendor;Model;SerialNumber;ControllerNumber;AdditionalInformation
    print "DISCOVERY;1;Tape Drive 1;1;Enclosure;Sony;S40;154ERD;1;Element ID: 500 Fibre Node Name: 500104f00094c3c4 Hardware Version: L29S"
    print "DISCOVERY;2;Tape Drive 2;1;Enclosure;IBM;TS2270;IBMTS2270TapeDrive;1;Element ID: 2270 Hardware Version: TS2270"
    print "DISCOVERY;3;Tape Drive 3;1;Enclosure;IBM;TS2280;IBMTS2280TapeDrive;1;Element ID: 500 Fibre Node Name: 500104f00094c3c4 Hardware Version: TS2280"

    # TapeDrive  Collect
    # TapeDrive  COLLECT;TdId;TdStatus;StatusInformation;MountCount;UnmountCount;ErrorCount;NeedsCleaning
    print "COLLECT;1;ok;All good;2;2;2;1;"
    print "COLLECT;2;failed;Critical failure;0;0;0;0;"
    print "COLLECT;3;UNKNOWN;;5;10;15;2;"
}