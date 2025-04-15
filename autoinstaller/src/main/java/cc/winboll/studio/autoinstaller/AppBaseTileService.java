package cc.winboll.studio.autoinstaller;

/**
 * @Author ZhanGSKen@AliYun.Com
 * @Date 2025/04/15 09:24:46
 * @Describe 磁贴工具服务类
 */
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import cc.winboll.studio.autoinstaller.models.AppConfigs;

public class AppBaseTileService extends TileService {

    public static final String TAG = "AppBaseTileService";

    @Override
    public void onStartListening() {
        super.onStartListening();
        Tile tile = getQsTile();
        tile.setIcon(android.graphics.drawable.Icon.createWithResource(this, R.drawable.ic_android));
        // 更新磁贴状态
        tile.setState(AppConfigs.getInstance(getApplicationContext()).isEnableService()?Tile.STATE_ACTIVE: Tile.STATE_INACTIVE);
        tile.updateTile();
    }

    @Override
    public void onClick() {
        super.onClick();
//        Tile tile = getQsTile();
//
//        if (tile.getState() == Tile.STATE_ACTIVE) {
//            tile.setState(Tile.STATE_INACTIVE);
//            AppConfigs.getInstance(AppBaseTileService.this).setIsEnableService(false);
//            AppConfigs.getInstance(AppBaseTileService.this).saveAppConfigs();
//            MainActivity.stopMainService();
//        } else if (tile.getState() == Tile.STATE_INACTIVE) {
//            tile.setState(Tile.STATE_ACTIVE);
//            AppConfigs.getInstance(AppBaseTileService.this).setIsEnableService(true);
//            AppConfigs.getInstance(AppBaseTileService.this).saveAppConfigs();
//            MainActivity.startMainService();
//        }
    }
}
