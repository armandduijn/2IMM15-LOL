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
	
    public function getTitle(): string
    {
        return 'Topic';
    }

    /**
     * @inheritdoc
     */
    public function render($data = []): string
    {
		if ($this->getAuthorId() !== NULL) {
			//Get the topics based on author id
			$data = file_get_contents(__DIR__ . "/../../../../modules/topic_modeling/topics/authorid_topics.txt");
			$json_data = json_decode($data, true);
			$topic_ids = $json_data[$this->getAuthorId()];
			//Get the wordcloud pictures
			$results = array();
			$i = 0;
			foreach ($topic_ids as $id) {
				$src_pic = "/img/topic-pics/wc-topic" . $id . ".png";
				if (!in_array($src_pic, $results)) {
					$topic_pic = __DIR__ . "/../../../public/img/topic-pics/wc-topic" . $id . ".png";
					if (!file_exists($topic_pic)) {
						$original_pic = __DIR__ . "/../../../../modules/topic_modeling/topics/topic-pics/wc-topic" . $id . ".png";
						if (file_exists($original_pic)) {
							copy($original_pic,$topic_pic);
							$results[$i] = "/img/topic-pics/wc-topic" . $id . ".png";
						}
					} else {
						$results[$i] = "/img/topic-pics/wc-topic" . $id . ".png";
					}
				}
				
				$i++;
			}
        } else {
			//Analyze the topics of the query
		}

        ob_start();

        extract($results);
        include __DIR__ . '/view/topic.phtml';

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
}
