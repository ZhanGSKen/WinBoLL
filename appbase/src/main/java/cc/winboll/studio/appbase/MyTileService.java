package cc.winboll.studio.appbase;

/**
 * @Author ZhanGSKen@AliYun.Com
 * @Date 2025/02/13 19:30:10
 */
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.widget.Toast;

public class MyTileService extends TileService {
    @Override
    public void onStartListening() {
        super.onStartListening();
        Tile tile = getQsTile();
        tile.setState(Tile.STATE_INACTIVE);
        tile.setLabel(getString(R.string.tileservice_name));
        tile.setIcon(android.graphics.drawable.Icon.createWithResource(this, R.drawable.ic_cloud_outline));
       

        tile.updateTile();
    }

    @Override
    public void onClick() {
        super.onClick();
        Toast.makeText(this, "磁贴被点击", Toast.LENGTH_SHORT).show();
        Tile tile = getQsTile();
        if (tile.getState() == Tile.STATE_INACTIVE) {
            tile.setState(Tile.STATE_ACTIVE);
            tile.setIcon(android.graphics.drawable.Icon.createWithResource(this, R.drawable.ic_cloud));

        } else {
            tile.setState(Tile.STATE_INACTIVE);
            tile.setIcon(android.graphics.drawable.Icon.createWithResource(this, R.drawable.ic_cloud_outline));

        }
        tile.updateTile();
    }

}
