BEGIN {

############################################### Value to be set manually ###############################################

    # DiskController DISCOVERY
    # DiskController DISCOVERY DISCOVERY;DiskControllerID;DiskControllerDisplayID;AttachedTodeviceID;Vendor;Model;ControllerNumber;AdditionalInformation
    print "DISCOVERY;1;Disk Controller 1;1;EMC .Company;Model 15;1;Manufacturer: FXN - Spare Part Number: 683245-001 - Product Number: QR482A - Alternative Serial Number: 0000000000 Bios: 4.7.8 - Kernel: 3.1.3"
    print "DISCOVERY;2;Disk Controller 2;1;EMC .Company;Model 15;2;Manufacturer: FXN - Spare Part Number: 683245-001 - Product Number: QR482A - Alternative Serial Number: CZ33169605 Bios: 4.7.8 - Kernel: 3.1.3"
    print "DISCOVERY;3;F5 Big;1;F5-BigIP;D106;;Manufacturer: BIG - Spare Part Number: F5-D106 - Product Number: D106 - Alternative Serial Number: f5-iryj-vgrt"

    # DiskController Collect
    # COLLECT;DiskControllerID;DiskControllerStatus;BatteryStatus
    print "COLLECT;1;ok;ok"
    print "COLLECT;2;failed;degraded"
    print "COLLECT;3;degraded;failed"
}