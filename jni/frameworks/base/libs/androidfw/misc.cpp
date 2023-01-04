/*
 * Copyright (C) 2005 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#define LOG_TAG "misc"

//
// Miscellaneous utility functions.
//
#include <androidfw/misc.h>

#include <sys/stat.h>
#include <cstring>
#include <errno.h>
#include <cstdio>

#include <android-base/utf8.h>

using namespace android;

/* 
 * If you use another encoding on Windows, you solved the problem that you can't open the APK.
 * The original location of the UTF8 namespace is 'android/system/libbase/utf8.cpp'.
 * The APK Scanner has been temporarily added here to manage with one project.
 * 
 * Another solution is not to use UTF8 path in the OpenArchive of zip_archive.
 * This was modified to use UTF8 to handle the resources of long paths.
 * If you want to use the 'aapt' command in the Windows Command window,
 * you may need to revert the following modifications.
 * refer to https://android.googlesource.com/platform/system/core/+/c77f9d380f93235a1094f8471824d67da5b7addd%5E%21/
 */
namespace android {
namespace base {
namespace utf8 {
#ifdef _WIN32
int stat(const char *name, struct stat *st) {
    std::wstring name_utf16;
    if (!::android::base::UTF8PathToWindowsLongPath(name, &name_utf16)) {
        return -1;
    }
    int err;
    struct _stat wstat;

    err = _wstat(name_utf16.c_str(), &wstat);
    if (!err) {
        st->st_mode = wstat.st_mode;
        st->st_mtime = wstat.st_mtime;
    }

    return err;
}
#else
using ::stat;
#endif
}
}

/*
 * Get a file's type.
 */
FileType getFileType(const char* fileName)
{
    struct stat sb;

    if (::android::base::utf8::stat(fileName, &sb) < 0) {
        if (errno == ENOENT || errno == ENOTDIR)
            return kFileTypeNonexistent;
        else {
            fprintf(stderr, "getFileType got errno=%d on '%s'\n",
                errno, fileName);
            return kFileTypeUnknown;
        }
    } else {
        if (S_ISREG(sb.st_mode))
            return kFileTypeRegular;
        else if (S_ISDIR(sb.st_mode))
            return kFileTypeDirectory;
        else if (S_ISCHR(sb.st_mode))
            return kFileTypeCharDev;
        else if (S_ISBLK(sb.st_mode))
            return kFileTypeBlockDev;
        else if (S_ISFIFO(sb.st_mode))
            return kFileTypeFifo;
#if defined(S_ISLNK)
        else if (S_ISLNK(sb.st_mode))
            return kFileTypeSymlink;
#endif
#if defined(S_ISSOCK)
        else if (S_ISSOCK(sb.st_mode))
            return kFileTypeSocket;
#endif
        else
            return kFileTypeUnknown;
    }
}

/*
 * Get a file's modification date.
 */
time_t getFileModDate(const char* fileName)
{
    struct stat sb;

    if (::android::base::utf8::stat(fileName, &sb) < 0)
        return (time_t) -1;

    return sb.st_mtime;
}

}; // namespace android
