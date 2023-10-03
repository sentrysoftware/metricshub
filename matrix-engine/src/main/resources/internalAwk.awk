function bytes2HumanFormatBase2(str)
{
	 if (str == "") {
	 	return ""
	}
	
    split("B;KiB;MiB;GiB;TiB;PiB;EiB", units)

    for (i = 1; str >= 1024 && i <= 7; i++) {
        str /= 1024
    }

    return sprintf("%.2f %s", str, units[i])
}

function bytes2HumanFormatBase10(str)
{
    if (str == "") {
	 	return ""
	}

	split("B;KB;MB;GB;TB;PB;EB", units)

    for (i = 1; str >= 1000 && i <= 7; i++) {
        str /= 1000
    }

    return sprintf("%.2f %s", str, units[i])
}

function mebiBytes2HumanFormat(str)
{
    if (str == "") {
	 	return ""
	}

	split("MiB;GiB;TiB;PiB;EiB", units)

    for (i = 1; str >= 1024 && i <= 5; i++) {
        str /= 1024
    }

    return sprintf("%.2f %s", str, units[i])
}

function megaHertz2HumanFormat(str)
{
    if (str == "") {
	 	return ""
	}

	split("Hz;KHz;MHz;GHz;THz;PHz;EHz", units)

    for (i = 1; str >= 1000 && i <= 7; i++) {
        str /= 1000
    }

    return sprintf("%.2f %s", str, units[i])
}

function join(sep, a, b, c) {
    return sprintf("%s%s%s%s%s", a, sep, b, sep, c)
}

BEGIN {
	FS = ";"
}

{
	printf SCRIPT_PLACEHOLDER
}
