package dev.slne.surf.cloud.core.data;

import lombok.experimental.UtilityClass;
import net.querz.nbt.tag.LongTag;
import net.querz.nbt.tag.NumberTag;

@UtilityClass
public class CloudPersistentData {

  public final long SERVER_ID_NOT_SET = -1L;

  public final PersistentData<Long> SERVER_ID = PersistentData.data(
      "server_id",
      LongTag.class,
      NumberTag::asLong,
      LongTag::new,
      SERVER_ID_NOT_SET
  );
}
