package org.wallentines.databridge;

import net.minecraft.world.entity.Entity;

public interface CommandSourceStackExtension {

    Entity getTriggerEntity();

    void setTriggerEntity(Entity entity);

}
