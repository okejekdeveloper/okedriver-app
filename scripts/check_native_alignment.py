"""Check 16 KB ELF load alignment in a built APK without extracting files."""
import argparse
import struct
import zipfile


def load_alignments(data):
    if data[:4] != b"\x7fELF":
        raise ValueError("Invalid ELF header")
    endian = "<" if data[5] == 1 else ">"
    if data[4] == 2:
        offset = struct.unpack_from(endian + "Q", data, 32)[0]
        entry_size, count = struct.unpack_from(endian + "HH", data, 54)
        format_string = endian + "IIQQQQQQ"
        alignment_index = 7
    else:
        offset = struct.unpack_from(endian + "I", data, 28)[0]
        entry_size, count = struct.unpack_from(endian + "HH", data, 42)
        format_string = endian + "IIIIIIII"
        alignment_index = 7
    return [entry[alignment_index] for index in range(count)
            if (entry := struct.unpack_from(format_string, data, offset + index * entry_size))[0] == 1]


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("apk")
    args = parser.parse_args()
    failed = []
    count = 0
    with zipfile.ZipFile(args.apk) as archive:
        for name in archive.namelist():
            if not name.endswith(".so") or not name.startswith(("lib/arm64-v8a/", "lib/x86_64/")):
                continue
            count += 1
            alignments = load_alignments(archive.read(name))
            if not alignments or min(alignments) < 16_384:
                failed.append(name)
                print(f"FAIL {name}: {alignments}")
    print(f"64-bit native libraries checked: {count}; failures: {len(failed)}")
    raise SystemExit(1 if failed else 0)


if __name__ == "__main__":
    main()
