package cc.winboll.studio.autoinstaller;
import android.content.Context;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import cc.winboll.studio.autoinstaller.models.MainServiceBean;
import cc.winboll.studio.autoinstaller.services.MainService;
import cc.winboll.studio.autoinstaller.models.AppConfigs;

/**
 * @Author ZhanGSKen@AliYun.Com
 * @Date 2025/04/15 09:24:46
 * @Describe 磁贴工具服务类
 */
public class AppBaseTileService extends TileService {
    public static final String TAG = "AppBaseTileService";

    volatile static AppBaseTileService _AppBaseTileService;

    @Override
    public void onStartListening() {
        super.onStartListening();
        _AppBaseTileService = this;
        Tile tile = getQsTile();
        //MainServiceBean bean = MainServiceBean.loadBean(this, MainServiceBean.class);
        
        if (AppConfigs.getInstance(AppBaseTileService.this).isEnableService()) {
            //MainService.startMainService(context);
            tile.setState(Tile.STATE_ACTIVE);
            tile.setIcon(android.graphics.drawable.Icon.createWithResource(this, R.drawable.ic_cloud));
        } else {
            //MainService.stopMainService(context);
            tile.setState(Tile.STATE_INACTIVE);
            tile.setIcon(android.graphics.drawable.Icon.createWithResource(this, R.drawable.ic_cloud_outline));
        }
        tile.updateTile();
//        Tile tile = getQsTile();
//        tile.setState(Tile.STATE_INACTIVE);
//        tile.setLabel(getString(R.string.tileservice_name));
//        tile.setIcon(android.graphics.drawable.Icon.createWithResource(this, R.drawable.ic_cloud_outline));
//        tile.updateTile();

    }

    @Override
    public void onClick() {
        super.onClick();
        Tile tile = getQsTile();
//        MainServiceBean bean = MainServiceBean.loadBean(this, MainServiceBean.class);
//        if (bean == null) {
//            bean = new MainServiceBean();
//        }

        if (tile.getState() == Tile.STATE_ACTIVE) {
//            bean.setIsEnable(false);
//            MainServiceBean.saveBean(this, bean);
            AppConfigs.getInstance(AppBaseTileService.this).setIsEnableService(false);
            AppConfigs.getInstance(AppBaseTileService.this).saveAppConfigs();
            MainActivity.stopMainService();
        } else if (tile.getState() == Tile.STATE_INACTIVE) {
            AppConfigs.getInstance(AppBaseTileService.this).setIsEnableService(true);
            AppConfigs.getInstance(AppBaseTileService.this).saveAppConfigs();
            MainActivity.startMainService();
        }
        updateServiceIconStatus(this);
    }

    public static void updateServiceIconStatus(Context context) {
        if (_AppBaseTileService == null) {
            return;
        }

        Tile tile = _AppBaseTileService.getQsTile();
        MainServiceBean bean = MainServiceBean.loadBean(context, MainServiceBean.class);
        if (bean != null && bean.isEnable()) {
            tile.setState(Tile.STATE_ACTIVE);
            tile.setIcon(android.graphics.drawable.Icon.createWithResource(context, R.drawable.ic_cloud));
        } else {
            tile.setState(Tile.STATE_INACTIVE);
            tile.setIcon(android.graphics.drawable.Icon.createWithResource(context, R.drawable.ic_cloud_outline));
        }
        tile.updateTile();
    }
}
