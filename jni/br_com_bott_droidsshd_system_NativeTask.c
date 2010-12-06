#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <fcntl.h>
#include "br_com_bott_droidsshd_system_NativeTask.h"
/* #include <android/log.h> */

JNIEXPORT jint JNICALL Java_br_com_bott_droidsshd_system_NativeTask_runCommand
  (JNIEnv *env, jclass class, jstring command)
{
  const char *commandString;
  commandString = (*env)->GetStringUTFChars(env, command, 0);
  int exitcode = system(commandString); 
  (*env)->ReleaseStringUTFChars(env, command, commandString);  
  return (jint)exitcode;
}

JNIEXPORT jint JNICALL Java_br_com_bott_droidsshd_system_NativeTask_chmod
  (JNIEnv *env, jclass class, jstring path, jint mode)
{
  const char *pathString = (*env)->GetStringUTFChars(env, path, 0);
  //  0 = success
  // -1 = failed to run fchmod
  // -2 = can't even open file
  int exitcode=-2;
  // apparently we don't have 'chmod' available on bionic
  // so we work around this by using 'fchmod' :-)
  int fd = open(pathString, O_RDONLY);
  if (fd) {
    exitcode = fchmod(fd, mode);
    close(fd);
  }
//  __android_log_print(ANDROID_LOG_DEBUG, "DroidSSHdNT", "PATH: [%s]", pathString);
//  __android_log_print(ANDROID_LOG_DEBUG, "DroidSSHdNT", "MODE DEC: [%d]", mode);
//  __android_log_print(ANDROID_LOG_DEBUG, "DroidSSHdNT", "MODE OCT: [%o]", mode);
  (*env)->ReleaseStringUTFChars(env, path, pathString);
  return (jint)exitcode;
}

JNIEXPORT jint JNICALL Java_br_com_bott_droidsshd_system_NativeTask_symlink
  (JNIEnv *env, jclass class, jstring original, jstring destination)
{
  const char *originalString;
  const char *destinationString;
  originalString = (*env)->GetStringUTFChars(env, original, 0);
  destinationString = (*env)->GetStringUTFChars(env, destination, 0);
  int exitcode = symlink(originalString, destinationString);
  (*env)->ReleaseStringUTFChars(env, original, originalString);
  (*env)->ReleaseStringUTFChars(env, destination, destinationString);
  return (jint)exitcode;
}

