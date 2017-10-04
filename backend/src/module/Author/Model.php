<?php

namespace App\Author;

use App\AbstractModel;
use PDO;

class Model extends AbstractModel
{
    /**
     * @var int|null
     */
    private $id;

    /**
     * @var string|null
     */
    private $name;

    /**
     * Model constructor.
     *
     * @param array $data
     */
    public function __construct(array $data = [])
    {
        $this->id   = $data['id']   ?? null;
        $this->name = $data['name'] ?? null;
    }

    /**
     * @param string $name
     * @return Model
     * @throws \Psr\Container\ContainerExceptionInterface
     * @throws \Psr\Container\NotFoundExceptionInterface
     */
    public function loadByName(string $name): self
    {
        /** @var PDO $connection */
        $connection = $this->getContainer()->get(PDO::class);

        $statement = $connection->prepare('SELECT * FROM authors WHERE name = :name LIMIT 1');
        $statement->execute([ ':name' => $name ]);

        $result = $statement->fetch();

        if (is_array($result)) {
            $this->id   = (int)    $result['id'];
            $this->name = (string) $result['name'];
        }

        return $this;
    }

    /**
     * @return int
     */
    public function getId(): int
    {
        return $this->id;
    }

    /**
     * @return string
     */
    public function getName(): string
    {
        return $this->name;
    }
}
