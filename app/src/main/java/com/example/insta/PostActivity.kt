package com.example.insta

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.insta.adapter.PostAdapter
import com.example.insta.models.Post
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.activity_post.*

private const val TAG = "PostActivity"
class PostActivity : AppCompatActivity() {

    private lateinit var fireStoreDb : FirebaseFirestore
    private lateinit var posts: MutableList<Post>
    private lateinit var adapter: PostAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post)

        // Create Data Sources
        posts = mutableListOf()

        // Create the Adapter
        adapter = PostAdapter(this, posts)

        // Bind the adapter and layout manager to the RV
        rvPosts.adapter = adapter
        rvPosts.layoutManager = LinearLayoutManager(this)

        fireStoreDb = FirebaseFirestore.getInstance()
        val postsReference = fireStoreDb
            .collection("posts")
            .limit(20)
            .orderBy("creation_time_ms", Query.Direction.DESCENDING)

        postsReference.addSnapshotListener { snapshot, exception ->
            if (exception != null || snapshot == null){
                Log.e(TAG, "Exception whiling querying posts", exception)
                return@addSnapshotListener
            }

            val postList = snapshot.toObjects(Post::class.java)
            posts.clear()
            posts.addAll(postList)
            adapter.notifyDataSetChanged()


            for(post in postList){
                Log.i(TAG, "Post ${post}")
            }
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_posts, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_profile){
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }
        return super.onOptionsItemSelected(item)
    }
}