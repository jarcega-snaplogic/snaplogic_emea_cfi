#!/usr/bin/awk -f
# Fast SnapLogic pipeline structure validation
# Single-pass validation for performance

BEGIN {
    snap_count = 0
    link_count = 0
    pipeline_class_found = 0
    snap_ids_found = 0
    link_dst_ids = ""
    link_src_ids = ""
    snap_map_ids = ""
    error_message = ""
}

# Track pipeline class
/"class_id": "com-snaplogic-pipeline"/ {
    pipeline_class_found = 1
}

# Count snaps (excluding pipeline itself)
/"class_id": "com-snaplogic-snaps-/ {
    snap_count++
}

# Count links and collect IDs
/"link[0-9]+": {/ {
    in_link = 1
    link_count++
}

# Collect destination IDs from links
in_link && /"dst_id": "([^"]+)"/ {
    match($0, /"dst_id": "([^"]+)"/, arr)
    if (arr[1]) {
        if (link_dst_ids == "") link_dst_ids = arr[1]
        else link_dst_ids = link_dst_ids "," arr[1]
    }
}

# Collect source IDs from links  
in_link && /"src_id": "([^"]+)"/ {
    match($0, /"src_id": "([^"]+)"/, arr)
    if (arr[1]) {
        if (link_src_ids == "") link_src_ids = arr[1]
        else link_src_ids = link_src_ids "," arr[1]
    }
}

# End of link block
in_link && /^[[:space:]]*},$/ {
    in_link = 0
}

# Track when we're in snap_map section
/"snap_map": {/ {
    in_snap_map = 1
}

# Collect snap_map IDs only from snap_map section
in_snap_map && /"11111111-1111-1111-1111-[0-9]{12}": {/ {
    match($0, /"(11111111-1111-1111-1111-[0-9]{12})"/, arr)
    if (arr[1]) {
        snap_ids_found++
        if (snap_map_ids == "") snap_map_ids = arr[1]
        else snap_map_ids = snap_map_ids "," arr[1]
    }
}

END {
    # Validation checks
    if (!pipeline_class_found) {
        print "Missing pipeline class_id"
        exit 1
    }
    
    # For pipelines with multiple snaps, expect (snap_count - 1) links
    if (snap_count > 1 && link_count != (snap_count - 1)) {
        printf "Link/snap mismatch: %d links for %d snaps (expected %d)\n", link_count, snap_count, (snap_count - 1)
        exit 1
    }
    
    # Basic UUID consistency check
    if (snap_ids_found != snap_count) {
        printf "Snap ID mismatch: found %d snap IDs for %d snaps\n", snap_ids_found, snap_count
        exit 1
    }
    
    # Success - no output for speed
    exit 0
}