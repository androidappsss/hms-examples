package pe.cgonzales.hmsdemoapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import pe.cgonzales.hmsdemoapp.account.HuaweiIdActivity
import pe.cgonzales.hmsdemoapp.iap.DemoActivity
import pe.cgonzales.hmsdemoapp.maps.MyMapActivity

class MainActivity : AppCompatActivity(), AdapterDelegate {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val myDataset = arrayOf(
            "Account",
            "In-App Purchases",
            "Maps",
            "abc",
            "xyz"
        )
        val viewManager = LinearLayoutManager(this)
        val viewAdapter = MyAdapter(myDataset, this)
        findViewById<RecyclerView>(R.id.recyclerView).apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
        }
    }

    override fun onClickItem(position: Int) {
        var intent: Intent? = null
        when (position) {
            0 -> {
                intent = Intent(this, HuaweiIdActivity::class.java)
            }
            1 -> {
                intent = Intent(this, DemoActivity::class.java)
            }
            2 -> {
                intent = Intent(this, MyMapActivity::class.java)
            }
        }
        if (intent != null) {
            startActivity(intent)
        }
    }
}
