<?php

namespace App\Topic;

use App\AbstractModule;
use App\RenderableInterface;
use PDO;

class Topic extends AbstractModule implements RenderableInterface
{
	/**
     * Author ID
     *
     * @var string
     */
    protected $authorId;
	protected $topicId;
	protected $year;
	protected $stats;
	
    public function getTitle(): string
    {
		if ($this->topicId !== NULL) {
			if ($this->stats !== NULL) {
				return 'Terms & evolution';
			} else {
				return 'Top 5 authors';
			}
		} else if ($this->year !== NULL) {
			if ($this->stats !== NULL) {
				return 'Statistics';
			} else {
				return 'Top 5 topics';
			}
		} else {
			return 'Topics';
		}
    }

    /**
     * @inheritdoc
     */
    public function render($data = []): string
    {
		if ($this->authorId !== NULL) {
			//Get the topics based on author id
			$data = file_get_contents(__DIR__ . "/../../../../modules/topic_modeling/topics/authorid_topics.txt");
			$json_data = json_decode($data, true);
			$topic_ids = $json_data[$this->getAuthorId()];
			//Get the wordcloud pictures
			$results = array();
			$i = 0;
			foreach ($topic_ids as $id) {
				$src_pic = "/img/topic-pics/wc-topic" . $id . ".png";
				if (!in_array($id, $results)) {
					$topic_pic = __DIR__ . "/../../../public/img/topic-pics/wc-topic" . $id . ".png";
					if (!file_exists($topic_pic)) {
						$original_pic = __DIR__ . "/../../../../modules/topic_modeling/topics/topic-pics/wc-topic" . $id . ".png";
						if (file_exists($original_pic)) {
							copy($original_pic,$topic_pic);
							$results[$i] = $id;
						}
					} else {
						$results[$i] = $id;
					}
				}
				
				$i++;
			}
			$page = '/view/topic.phtml';
		} else if ($this->topicId !== NULL) {
			if ($this->stats !== NULL) {
				//Get the word cloud
				$id = $this->topicId;
				$results = array();
				$src_pic = "/img/topic-pics/wc-topic" . $id . ".png";
				$topic_pic = __DIR__ . "/../../../public/img/topic-pics/wc-topic" . $id . ".png";
				if (!file_exists($topic_pic)) {
					$original_pic = __DIR__ . "/../../../../modules/topic_modeling/topics/topic-pics/wc-topic" . $id . ".png";
					if (file_exists($original_pic)) {
						copy($original_pic,$topic_pic);
						$results[0] = $src_pic;
					}
				} else {
					$results[0] = $src_pic;
				}
				//Get the evolution graph
				$src_pic = "/img/topic-pics/" . $id . ".jpg";
				$topic_pic = __DIR__ . "/../../../public/img/topic-pics/" . $id . ".jpg";
				if (!file_exists($topic_pic)) {
					$original_pic = __DIR__ . "/../../../../modules/topic_modeling/topics/topic-pics/" . $id . ".jpg";
					if (file_exists($original_pic)) {
						copy($original_pic,$topic_pic);
						$results[1] = $src_pic;
					}
				} else {
					$results[1] = $src_pic;
				}
				$page = '/view/terms.phtml';
			} else {
				//Get the top authors
				$command = "python \"" . __DIR__ . "/../../../../modules/view-helpers/topic.py\" ";
				$command .= escapeshellarg($_GET['i']);
				$output = shell_exec($command);
			
				$author_ids = array_map('intval', explode(',', $output));
				
				// retrieve info for authors
				$db = new PDO('sqlite:'. __DIR__ . '/../../../../data/database.sqlite');
				$statement = $db->prepare("SELECT * from authors WHERE authors.id = :id");
				$results = [];
				foreach ($author_ids as $id) {
					$statement->execute([':id' => $id]);
					$rows = $statement->fetchAll();
					$results[$id]['name'] = $rows[0]['name'];
				}
				
				$page = '/view/authors.phtml';
			}
		} else if ($this->year !== NULL) {
			if ($this->stats !== NULL) {
				//Get the graphs for the statistics of this year's topics
				$results = [];
				$id = $this->year;
				$src_pic = "/img/topic-pics/year" . $id . ".jpg";
				$graph_pic = __DIR__ . "/../../../public/img/topic-pics/year" . $id . ".jpg";
				if (!file_exists($graph_pic)) {
					$original_pic = __DIR__ . "/../../../../modules/topic_modeling/topics/topic-pics/" . $id . ".jpg";
					if (file_exists($original_pic)) {
						copy($original_pic,$graph_pic);
						$results[0] = $src_pic;
					}
				} else {
					$results[0] = $src_pic;
				}
				
				$page = '/view/stats.phtml';				
			} else {
				//Get the top 5 topics and evolution graph of this year
				$command = "python \"" . __DIR__ . "/../../../../modules/view-helpers/topic.py\" ";
				$command .= escapeshellarg($_GET['i']);
				$output = shell_exec($command);
			
				$topic_ids = array_map('intval', explode(',', $output));
				
				$results = [];
				$i = 0;
				foreach ($topic_ids as $id) {
					$src_pic = "/img/topic-pics/wc-topic" . $id . ".png";
					if (!in_array($src_pic, $results)) {
						$topic_pic = __DIR__ . "/../../../public/img/topic-pics/wc-topic" . $id . ".png";
						if (!file_exists($topic_pic)) {
							$original_pic = __DIR__ . "/../../../../modules/topic_modeling/topics/topic-pics/wc-topic" . $id . ".png";
							if (file_exists($original_pic)) {
								copy($original_pic,$topic_pic);
								$results[$i] = $id;
							}
						} else {
							$results[$i] = $id;
						}
					}
					
					$i++;
				}
				
				$page = '/view/topic.phtml';
			}
        } else {
			//Analyze the topics of the documents fetched by the query
			$command = "python \"" . __DIR__ . "/../../../../modules/view-helpers/query.py\" ";
			$command .= escapeshellarg($_GET['i']);
			$output = shell_exec($command);
			$documentIds = array_map('intval', explode(',', $output));
			
			//Get the json mapping of docid-topicId
			$data = file_get_contents(__DIR__ . "/../../../../modules/topic_modeling/topics/docid_topicid.txt");
			$json_data = json_decode($data, true);
			
			//Get the topics for the first 10 documents
			$results = [];
			$i = 0;
			$docCount = 0;
			foreach($documentIds as $id) {
				if ($docCount == 9)
					break;
				$topic_ids = $json_data[$id];
				foreach ($topic_ids as $topicid) {
					if (!in_array($topicid,$results)) {
						$results[$i] = $topicid;
						$i++;
					}
				}
				$docCount++;
			}
			
			$page = '/view/topic.phtml';
		}

        ob_start();

        extract($results);
        include __DIR__ . $page;

        return ob_get_clean();
    }

    /**
     * @return string
     */
    public function getAuthorId(): string
    {
        return $this->authorId;
    }

    /**
     * @param string $authorId
     */
    public function setAuthorId(string $authorId)
    {
        $this->authorId = $authorId;
    }

    /**
     * @return string
     */
    public function getTopicId(): string
    {
        return $this->topicId;
    }

    /**
     * @param string $topicId
     */
    public function setTopicId(string $topicId)
    {
        $this->topicId = $topicId;
    }

    /**
     * @return string
     */
    public function getYear(): string
    {
        return $this->year;
    }

    /**
     * @param string $year
     */
    public function setYear(string $year)
    {
        $this->year = $year;
    }

    /**
     * @param string $stat
     */
    public function setStats(bool $stats)
    {
        $this->stats = $stats;
    }
}
