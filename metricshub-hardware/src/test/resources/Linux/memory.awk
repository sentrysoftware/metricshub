/MemTotal/ {
    memTotal = $2 * 1024
}

/MemFree/ {
    memFree = $2 * 1024
    memFreeUtilization = memFree / memTotal
}

/Buffers/ {
    memBuffers = $2 * 1024
    memBuffersUtilization = memBuffers / memTotal
}

/^Cached/ {
    memCached = $2 * 1024
    memCachedUtilization = memCached / memTotal
    memUsed = memTotal - memCached - memFree
    memUsedUtilization = memUsed / memTotal
}

END {
    printf("%s;%s;%s;%s;%s;%s;%s;%s;%s\n", memTotal, memFree, memUsed, memBuffers, memCached, memFreeUtilization, memUsedUtilization, memBuffersUtilization, memCachedUtilization)
}