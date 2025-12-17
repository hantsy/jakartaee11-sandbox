package com.example.blog;

import com.example.domain.Comment;
import com.example.domain.Post;
import com.example.domain.PostSummary;
import com.example.domain.Status;
import jakarta.data.Limit;
import jakarta.data.Order;
import jakarta.data.page.Page;
import jakarta.data.page.PageRequest;
import jakarta.data.repository.*;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Transactional
@Repository
public interface Blogger {


    // the projection is not available in Jakarta Data 1.0.
    // Jakarta Data 1.1 will cover it,
    // see: https://jakarta.ee/specifications/data/1.1/jakarta-data-1.1.0-m1#_select_clause
//    @Query("""
//            SELECT p.id, p.title FROM Post AS p
//            WHERE p.title LIKE :title
//            ORDER BY p.createdAt DESC
//            """)
//    Page<PostSummary> allPosts(@Param("title") String title, PageRequest page);

//    @Query("""
//            SELECT new com.example.domain.PostSummary(p.id, p.title) FROM Post AS p
//            WHERE p.title LIKE :title
//            ORDER BY p.createdAt DESC
//            """)
    @Query("""
            SELECT p FROM Post p
            WHERE p.title LIKE :title
            ORDER BY p.createdAt DESC
            """)
    Page<Post> allPosts(@Param("title") String title, PageRequest page);

    @Find
    @OrderBy("createdAt")
    List<Post> byStatus(Status status, Order<Post> order, Limit limit);

    @Find
    Optional<Post> byId(UUID id);

    @Insert
    Post insert(Post post);

    @Update
    Post update(Post post);

    @Delete
    void delete(Post post);

    // see: https://hibernate.zulipchat.com/#narrow/stream/132096-hibernate-user/topic/Jakarta.20Data.20cascade.20does.20not.20work.20in.20custom.20deletion.20Query/near/441874793
    @Query("delete from Post")
    @Transactional
    long deleteAllPosts();

//
//    StatelessSession session();
//
//    default List<Comment> getCommentsOfPost(UUID postId) {
//        var post = this.byId(postId).orElseThrow(() -> new PostNotFoundException(postId));
//        session().fetch(post.getComments());
//        return post.getComments();
//    }

    @Insert
    Comment insert(Comment comment);
}
