<?php

namespace App;

use Psr\Container\ContainerInterface;

abstract class AbstractModule implements ContainerAwareInterface
{
    protected $title = 'Component';

    /**
     * @var
     */
    private $container;

    /**
     * @param ContainerInterface|null $container
     * @return void
     */
    public function setContainer(ContainerInterface $container = null): void
    {
        $this->container = $container;
    }

    /**
     * @return ContainerInterface
     */
    protected function getContainer(): ContainerInterface
    {
        return $this->container;
    }

    public function getTitle(): string
    {
        return $this->title;
    }

    /**
     * @param string $title
     */
    public function setTitle(string $title)
    {
        $this->title = $title;
    }
}
