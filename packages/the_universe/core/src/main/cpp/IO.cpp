



#include "IO.h"
#include "Log.h"
#include <cstring>
#include <mutex>

jmethodID getAbsolutePathMethodId;

static list<IO::RelocateInfo> relocate_rule;
static std::mutex relocate_mutex;

const char *IO::redirectPath(const char *__path) {
    if (__path == nullptr) return nullptr;
    
    if (strstr(__path, "resource-cache")) {
        ALOGD("Blocking resource-cache path: %s", __path);
        return "/dev/null";
    }
    
    
    if (strstr(__path, "@idmap")) {
        ALOGD("Blocking idmap path: %s", __path);
        return "/dev/null";
    }
    
    
    if (strstr(__path, "systemui") && (strstr(__path, ".frro") || strstr(__path, "-accent-") || strstr(__path, "-dynamic-") || strstr(__path, "-neutral-"))) {
        ALOGD("Blocking systemui problematic path: %s", __path);
        return "/dev/null";
    }
    
    
    if (strstr(__path, "data@resource-cache@")) {
        ALOGD("Blocking data@resource-cache@ pattern: %s", __path);
        return "/dev/null";
    }
    
    
    if (strstr(__path, ".frro")) {
        ALOGD("Blocking .frro file: %s", __path);
        return "/dev/null";
    }
    
    
    if (strstr(__path, "systemui")) {
        ALOGD("Blocking systemui path: %s", __path);
        return "/dev/null";
    }

    const std::string path(__path);
    std::lock_guard<std::mutex> guard(relocate_mutex);
    const RelocateInfo *match = nullptr;
    for (const auto &rule : relocate_rule) {
        const size_t length = rule.targetPath.size();
        if (path.compare(0, length, rule.targetPath) == 0 &&
            (path.size() == length || rule.targetPath.back() == '/' || path[length] == '/') &&
            (match == nullptr || length > match->targetPath.size())) {
            match = &rule;
        }
    }
    if (match != nullptr) {
        thread_local std::string redirected;
        redirected = match->relocatePath + path.substr(match->targetPath.size());
        return redirected.c_str();
    }
    return __path;
}

jstring IO::redirectPath(JNIEnv *env, jstring path) {




    return UniverseNativeCore::redirectPathString(env, path);
}

jobject IO::redirectPath(JNIEnv *env, jobject path) {






    return UniverseNativeCore::redirectPathFile(env, path);
}

void IO::addRule(const char *targetPath, const char *relocatePath) {
    if (targetPath == nullptr || targetPath[0] == '\0' || relocatePath == nullptr) return;
    std::lock_guard<std::mutex> guard(relocate_mutex);
    for (auto &rule : relocate_rule) {
        if (rule.targetPath == targetPath) {
            rule.relocatePath = relocatePath;
            return;
        }
    }
    relocate_rule.push_back({targetPath, relocatePath});
}

void IO::init(JNIEnv *env) {
    jclass tmpFile = env->FindClass("java/io/File");
    getAbsolutePathMethodId = env->GetMethodID(tmpFile, "getAbsolutePath", "()Ljava/lang/String;");
}
