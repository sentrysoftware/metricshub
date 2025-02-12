BEGIN {

############################################### Value to be set manually ###############################################

    maxdisk=250;
    maxsize=300000000000;
    i=1;
    # DISCOVERY WITH TABLEJOINT
    # JOIN1;PdID
    while (i<=maxdisk) {
        physicaldisks[$i]="JOIN1;"i";";
        print physicaldisks[$i]
        i=i+1;
    }
    print "JOIN1;"500";";
    print "JOIN1;"501";";
    print "JOIN1;"502";";
    print "JOIN1;"503";";

    i=1;
    # DISCOVERY WITH TABLEJOINT
    # JOIN2;PdID;PdDisplayId;AttachedToDeviceID;AttachedToDeviceType;ControllerNumber;Vendor;Model;FirmwareVersion;Size;SerialNumber;AdditionalInformation
    while (i<=maxdisk) {
        disksizefunction = int(i * (maxsize/maxdisk));
        physicaldisks[$i]="JOIN2;"i";Disk "i";1;Enclosure;1;Western Digital;Modeltest;12E;"disksizefunction";154ERFD455;Slot : B"i
        print physicaldisks[$i]
        i=i+1;
    }
    # FOR UNIT TEST
    # COLLECT;PdID;PdStatus;StatusInformation;PredictedFailure;ErrorCount;DeviceNotReadyErrorCount;MediaErrorCount;NoDeviceErrorCount;HardErrorCount;IllegalRequestErrorCount;RecoverableErrorCount;TransportErrorCount

    print "JOIN2;"500";Disk "500";1;Enclosure;1;SEAGATE;H101860SFSUN600G;12E;5001514;D3006RGL;Slot : B"500
    print "JOIN2;"501";Disk "501";1;Enclosure;1;IBM-ESXS;ST2000NXCLAR2000;13E;10000000;6SE40JGC;Slot : B"501
    print "JOIN2;"502";Disk "502";1;Enclosure;1;Western Digital;Modeltest;14E;999999999999;H101860;Slot : B"502
    print "JOIN2;"503";Disk "503";1;Enclosure;1;Crucial;MX500;MX500v2;545154;LEC88FOL;Slot : B"503

    i=1;
    ## COLLECT
    while (i<=maxdisk) {
        diskendurancefunction = int(i * (100 / maxdisk));
        if ( i > (maxdisk/2) )
        {
            physicaldisks[$i]="COLLECT;"i";ok;Disk OK;True;1;1;1;41;1;1;1;1;"diskendurancefunction
        }
        else
        {
            physicaldisks[$i]="COLLECT;"i";ok;Disk OK;False;1;1;1;41;1;1;1;1;"diskendurancefunction
        }

        print physicaldisks[$i]
        i=i+1;
    }
    # FOR UNIT TEST
    print "COLLECT;"500";ok;Disk OK;False;1;1;1;41;1;1;1;1;"1
    print "COLLECT;"501";ok;Disk OK;False;1;1;1;41;1;1;1;1;"5
    print "COLLECT;"502";ok;Disk OK;False;1;1;1;41;1;1;1;1;"10
    print "COLLECT;"503";ok;Disk OK;False;15;20;30;40;50;60;70;80;"1

}