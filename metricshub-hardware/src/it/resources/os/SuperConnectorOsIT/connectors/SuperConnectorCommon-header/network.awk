BEGIN {

############################################### Value to be set manually ###############################################
    # NetworkCard  Discovery
    # NetworkCard  DISCOVERY;NCid;NCdisplatId;AttachedToDeviceID;Vendor;Model;NCtype;PhysicalAddress;LogicalAddress;LogicalAddressType;SN;AdditionalInformation
    print "DISCOVERY;1;Network Card 1;1;Gigabyt;RG1G;ETHERNET;00:0a:95:9d:68:16;192.168.5.24;IP;D4566ES45;Location: IO Module 2 Port ID 0 Part Number: 019-078-041 - Serial Number: 00000000000 - Alternative Part Number: AFBR-703ASDZ-E2 - Alternative Serial Number: AGL1403D1000664"
    print "DISCOVERY;2;Network Card 2;1;HP;GigaHP;FC Port;00:0a:95:9d:68:16;192.168.5.25;IP;D4566ES46;Location: IO Module 2 Port ID 0 Part Number: 019-078-041 - Serial Number: 00000000000 - Alternative Part Number: AFBR-703ASDZ-E2 - Alternative Serial Number: AGL1403D1000687"
    print "DISCOVERY;3;Network Card 3;1;AZUS;AZ100G;FC Port G;00:0a:95:9d:68:16;192.168.5.26;IP;D4566ES47;Location: IO Module 2 Port ID 1 Part Number: 019-078-041 - Serial Number: 00000000000 - Alternative Part Number: AFBR-703ASDZ-E2 - Alternative Serial Number: AGL1403D1000657 "
    print "DISCOVERY;4;MSHW-4791;1;AZUS;AZ100G;FC Port G;00:0a:95:9d:68:16;192.168.5.26;IP;D4566ES48;Location: IO Module 2 Port ID 1 Part Number: 019-078-041 - Serial Number: 00000000000 - Alternative Part Number: AFBR-703ASDZ-E2 - Alternative Serial Number: AGL1403D1001186 "

    # NetworkCard  Collect
    # NetworkCard  COLLECT;NCid;NCstatus;NSlinkStatus;NClinkSpeed;DuplexMode;ErrorCount;ReceivedBytes;TransmittedBytes;ReceivedPackets;TransmittedPackets;ZeroBufferCreditCount
    print "COLLECT;1;ok;ok;1000;1000;5;5222;4111"
    print "COLLECT;2;ok;ok;1000;1000;5;5222;4111"
    print "COLLECT;3;ok;ok;1000;1000;5;5222;4111;0;0;0;"
      print "COLLECT;4;ok;ok;56000;1;10;10000000;40000000;10;40;95"
}