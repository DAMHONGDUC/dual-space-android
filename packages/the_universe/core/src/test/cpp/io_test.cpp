#include "IO.h"
#include <cassert>
#include <cstring>
#include <string>
#include <thread>
#include <vector>

// JNI overloads are exercised on Android; the host test covers native rule ownership.
jstring UniverseNativeCore::redirectPathString(JNIEnv *, jstring path) { return path; }
jobject UniverseNativeCore::redirectPathFile(JNIEnv *, jobject path) { return path; }

int main() {
    char source[] = "/data/game";
    char destination[] = "/virtual/game";
    IO::addRule(source, destination);
    source[1] = 'X';
    destination[1] = 'X';
    assert(std::string(IO::redirectPath("/data/game/files")) == "/virtual/game/files");
    IO::addRule("/data/game/lib", "/native");
    assert(std::string(IO::redirectPath("/data/game/lib/a.so")) == "/native/a.so");
    assert(std::string(IO::redirectPath("/data/game2/files")) == "/data/game2/files");
    assert(std::string(IO::redirectPath("/data/game/files/data/game")) ==
           "/virtual/game/files/data/game");
    assert(IO::redirectPath(static_cast<const char *>(nullptr)) == nullptr);
    IO::addRule(nullptr, "/ignored");
    IO::addRule("", "/ignored");
    IO::addRule("/data/game", "/replacement");
    assert(std::string(IO::redirectPath("/data/game")) == "/replacement");

    std::vector<std::thread> workers;
    for (int i = 0; i < 4; ++i) {
        workers.emplace_back([] {
            for (int iteration = 0; iteration < 1000; ++iteration) {
                IO::addRule("/concurrent", "/safe");
                assert(std::string(IO::redirectPath("/concurrent/file")) == "/safe/file");
            }
        });
    }
    for (auto &worker : workers) worker.join();
}
