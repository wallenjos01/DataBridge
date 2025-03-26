package org.wallentines.databridge.impl;

import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public interface EntitySelectorExtension {

    void setTriggerEntity(boolean triggerEntity);

    boolean isTriggerEntity();

}
