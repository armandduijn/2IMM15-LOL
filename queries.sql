-- Amount of collaborations an author has done
SELECT A.id, A.name, count(PA2.author_id)
FROM authors AS A, paper_authors AS PA1, paper_authors AS PA2
WHERE PA1.author_id = A.id
      AND PA2.author_id <> A.id
      AND PA1.paper_id = PA2.paper_id
GROUP BY A.id
ORDER BY A.id;

-- Who an author has worked with and on which paper
SELECT A1.id, A1.name, PA2.author_id AS collaborator_id, A2.name AS collaborator_name, PA1.paper_id AS collaboration_paper_id, P.title AS collaboration_paper_title
FROM authors AS A1, authors AS A2, paper_authors AS PA1, paper_authors AS PA2, papers AS P
WHERE PA1.author_id = A1.id
      AND PA2.author_id = A2.id
      AND PA2.author_id <> A1.id
      AND PA1.paper_id = PA2.paper_id
      AND PA1.paper_id = P.id
ORDER BY A1.id;

-- Amount of collaborations with a certain author
SELECT A1.id, A1.name, PA2.author_id AS collaborator_id, A2.name AS collaborator_name, count(DISTINCT PA2.paper_id) AS amount_of_collaborations
FROM authors AS A1, authors AS A2, paper_authors AS PA1, paper_authors AS PA2
WHERE PA1.author_id = A1.id
      AND PA2.author_id = A2.id
      AND PA2.author_id <> A1.id
      AND PA1.paper_id = PA2.paper_id
GROUP BY A1.id, A2.id;
