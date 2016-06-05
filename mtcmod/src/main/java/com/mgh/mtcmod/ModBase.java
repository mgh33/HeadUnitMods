package com.mgh.mtcmod;

import de.robv.android.xposed.IXposedHookZygoteInit;


public abstract class ModBase implements IXposedHookZygoteInit {

    protected String mod_path = null;

    protected final String pkgSysUI = "com.android.systemui";
    protected final String appSysUI = pkgSysUI + ".SystemUIService";

    @Override
    public void initZygote(StartupParam startupParam) throws Throwable {
        mod_path = startupParam.modulePath;

    }

}
