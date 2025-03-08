package com.example.boscobel_accounting


import android.os.Bundle

import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.example.boscobel_accounting.R
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

lateinit var toolbar :androidx.appcompat.widget.Toolbar

class MainActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var toggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Drawerの設定
        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)

        // Toolbarの設定
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        // Toolbar右上のメニューはFragmentの中で生成

        // DrawerとToolbarをリンクさせるトグルボタンを作成
        toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_drawer, R.string.close_drawer)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // Drawerの各項目が選択された時の処理　NavigationViewの項目が選択されたときの処理
        navView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_accountingdata -> {
                    Toast.makeText(this, "Accounting Data selected", Toast.LENGTH_SHORT).show()
                    toolbar.title = "損益データ"
                    Fragment_AccountingData()
                }
                R.id.nav_accountingitem -> {
                    Toast.makeText(this, "Accounting Item selected", Toast.LENGTH_SHORT).show()
                    toolbar.title = "勘定科目マスター"
                    Fragment_AccountingItem()
                }
                R.id.nav_companydata -> {
                    Toast.makeText(this, "Company Data selected", Toast.LENGTH_SHORT).show()
                    toolbar.title = "会社データ"
                    Fragment_CompanyData()
                }
            }
            drawerLayout.closeDrawers() // メニューを閉じる
            true
        }


    }

    fun Fragment_AccountingData(){
        val fragment = PaLFragment()
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, fragment)
        transaction.addToBackStack(null) // オプションでバックスタックに追加
        transaction.commit()
    }

    fun Fragment_AccountingItem(){
        val fragment = AccountItemFragment()
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, fragment)
        transaction.addToBackStack(null) // オプションでバックスタックに追加
        transaction.commit()
    }

    fun Fragment_CompanyData(){
        val fragment = CompanyDataFragment()
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, fragment)
        transaction.addToBackStack(null) // オプションでバックスタックに追加
        transaction.commit()
    }

}

