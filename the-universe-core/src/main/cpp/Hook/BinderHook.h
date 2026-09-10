



#ifndef THE_UNIVERSE_BINDERHOOK_H
#define THE_UNIVERSE_BINDERHOOK_H


#include "BaseHook.h"

class BinderHook : public BaseHook{
public:
    static void init(JNIEnv *env);
};

#endif 
