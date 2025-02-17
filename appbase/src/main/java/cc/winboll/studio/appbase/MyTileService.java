package cc.winboll.studio.appbase;

/**
 * @Author ZhanGSKen@AliYun.Com
 * @Date 2025/02/13 19:30:10
 */
import android.content.Context;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import cc.winboll.studio.appbase.beans.MainServiceBean;
import cc.winboll.studio.appbase.services.MainService;

public class MyTileService extends TileService {
    public static final String TAG = "MyTileService";

    volatile static MyTileService _MyTileService;

    @Override
    public void onStartListening() {
        super.onStartListening();
        _MyTileService = this;
        Tile tile = getQsTile();
        MainServiceBean bean = MainServiceBean.loadBean(this, MainServiceBean.class);
        if (bean != null && bean.isEnable()) {
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
        MainServiceBean bean = MainServiceBean.loadBean(this, MainServiceBean.class);
        if (bean == null) {
            bean = new MainServiceBean();
        }

        if (tile.getState() == Tile.STATE_ACTIVE) {
            bean.setIsEnable(false);
            MainServiceBean.saveBean(this, bean);
            MainService.stopMainService(this);
        } else if (tile.getState() == Tile.STATE_INACTIVE) {
            bean.setIsEnable(true);
            MainServiceBean.saveBean(this, bean);
            MainService.startMainService(this);
        }
        updateServiceIconStatus(this);
    }

    public static void updateServiceIconStatus(Context context) {
        if (_MyTileService == null) {
            return;
        }

        Tile tile = _MyTileService.getQsTile();
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
