
#ifndef THE_UNIVERSE2_DEXFILEHOOK_H
#define THE_UNIVERSE2_DEXFILEHOOK_H

#include "BaseHook.h"

class DexFileHook : public BaseHook{
public:
    static void init(JNIEnv *env);
    static void setFileReadonly(const char* filePath);
};


#endif 
